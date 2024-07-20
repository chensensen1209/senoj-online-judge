package com.sen.senojbackendvoucherservice.utils;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.hmdp.entity.Shop;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @ClassDescription:
 * @Author: chensen
 * @Created: 2024/7/7 19:42
 */
@Slf4j
@Component
public class CacheClient {

    private StringRedisTemplate stringRedisTemplate;

    public CacheClient(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    //设置redis缓存
    public void set(String key, Object object, Long time, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(object), time, unit);
    }

    //设置带逻辑时间的缓存
    public void setLogicalExpire(String key, Object object, Long time, TimeUnit unit) {
        RedisData redisData = new RedisData();
        redisData.setData(object);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
    }

    //设置null制解决内存穿透方法重建缓存查询
    public <R, ID> R queryByWithThoughPass(String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit) {
        //从redis中查询
        String key = keyPrefix + id;
        String json = stringRedisTemplate.opsForValue().get(key);
        // NULL "" 都是Blank 有商铺数据才是true
        if (StrUtil.isNotBlank(json)) {
            R r = JSONUtil.toBean(json, type);
            return r;
        }
        //判断是否为空值
        if (json != null) {
            return null;
        }
        //没有查询到去数据库查,getbyID默认的从数据库中查询
        R r = dbFallback.apply(id);
        if (r == null) {
            //解决缓存击穿
            //1将空值写入redis并限制时间
            stringRedisTemplate.opsForValue().set(key, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
            //2返回错误信息
            return null;
        }
        //写入redis缓存
        this.set(key, r, time, unit);
        return r;
    }

    //设置逻辑时间来解决缓存击穿，查询方法
    //建立线程池
    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    public <R, ID> R queryByIdWithLocalExpire(String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit) {
        //1 从redis中查询
        String key = keyPrefix + id;
        String json = stringRedisTemplate.opsForValue().get(key);
        // 2 不存在返回null
        if (StrUtil.isBlank(json)) {
            return null;
        }
        // 3 命中缓存，反序列化
        RedisData redisData = JSONUtil.toBean(json, RedisData.class);
        JSONObject shopJson = (JSONObject) redisData.getData();
        R r = JSONUtil.toBean(shopJson, type);
        LocalDateTime expireTime = redisData.getExpireTime();
        // 4 判断是否过期
        if (expireTime.isAfter(LocalDateTime.now())) {
            // 4.1未过期，返回信息
            return r;
        }
        // 4.2过期，缓存重建
        // 5 缓存重建
        // 5.1 获取互斥锁
        String lockKey = RedisConstants.LOCK_SHOP_KEY + id;
        boolean isLock = tryLock(lockKey);
        // 5.2 判断锁是否获取成功
        if (isLock) {
            //获取锁成功检测一下redis中缓存是否存在，如果存在就不用重建缓存了
            String mutexShopJson = stringRedisTemplate.opsForValue().get(key);
            if (StrUtil.isNotBlank(mutexShopJson)) {
                r = JSONUtil.toBean(mutexShopJson, type);
                return r;
            }
            //判断是否为空值,之前缓存穿透设置的null
            if (mutexShopJson != null) {
                return null;
            }
            // 5.3 成功开启独立线程，实现缓存重建
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                // 缓存重建，方便测试后续改为分钟
                try {
                    //查询数据库
                    R r1 = dbFallback.apply(id);
                    //写入redis
                    this.setLogicalExpire(key, r1, time, unit);

                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    // 释放锁
                    unLock(lockKey);
                }
            });
        }
        // 5.4 返回过期的商铺信息
        return r;
    }

    public boolean tryLock(String key) {
        //完成一些重建要五百毫秒
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 500, TimeUnit.MILLISECONDS);
        return BooleanUtil.isTrue(flag);
    }

    //删除缓存互斥锁
    public void unLock(String key) {
        stringRedisTemplate.delete(key);
    }


}
