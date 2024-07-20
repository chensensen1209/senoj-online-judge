package com.sen.senojbackendvoucherservice.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.CacheClient;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.RedisData;
import com.hmdp.utils.SystemConstants;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisCommands;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisCommand;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private CacheClient cacheClient;

    @Override
    public Result queryById(Long id) {
        // 缓存穿透
        // Shop shop = queryByWithThoughPass(id);
//        /* 调用方法类
        Shop shop = cacheClient.queryByWithThoughPass(RedisConstants.CACHE_SHOP_KEY, id,
                Shop.class, this::getById, RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);

//        Shop shop = cacheClient.queryByIdWithLocalExpire(RedisConstants.CACHE_SHOP_KEY, id,
//                Shop.class, this::getById, RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);
        // 互斥锁解决缓存击穿
        // Shop shop = queryByIdWithMutex(id);
        // 逻辑过期解决缓存击穿
//        Shop shop = queryByIdWithLocalExpire(id);
        if (shop == null)
            return Result.fail("商家不存在");
        return Result.ok(shop);
    }

    //建立线程池
    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    public Shop queryByIdWithLocalExpire(Long id) {
        //1 从redis中查询
        String key = RedisConstants.CACHE_SHOP_KEY + id;
        String redisDataJson = stringRedisTemplate.opsForValue().get(key);
        // 2 不存在返回null
        if (StrUtil.isBlank(redisDataJson)) {
            return null;
        }
        // 3 命中缓存，反序列化
        RedisData redisData = JSONUtil.toBean(redisDataJson, RedisData.class);
        JSONObject shopJson = (JSONObject) redisData.getData();
        Shop shop = JSONUtil.toBean(shopJson, Shop.class);
        LocalDateTime expireTime = redisData.getExpireTime();
        // 4 判断是否过期
        if (expireTime.isAfter(LocalDateTime.now())) {
            // 4.1未过期，返回信息
            return shop;
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
                shop = JSONUtil.toBean(mutexShopJson, Shop.class);
                return shop;
            }
            //判断是否为空值,之前缓存穿透设置的null
            if (mutexShopJson != null) {
                return null;
            }
            // 5.3 成功开启独立线程，实现缓存重建
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                // 缓存重建，方便测试后续改为分钟
                try {
                    this.saveShop2Redis(id, 20L);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    // 释放锁
                    unLock(lockKey);
                }

            });
        }
        // 5.4 返回过期的商铺信息
        return shop;
    }

    //通过互斥锁访问缓存
    public Shop queryByIdWithMutex(Long id) {
        //从redis中查询
        String key = RedisConstants.CACHE_SHOP_KEY + id;
        String redisShopJson = stringRedisTemplate.opsForValue().get(key);
        // NULL "" 都是Blank 有商铺数据才是true
        if (StrUtil.isNotBlank(redisShopJson)) {
            Shop shop = JSONUtil.toBean(redisShopJson, Shop.class);
            return shop;
        }
        //判断是否为空值,之前缓存穿透设置的null
        if (redisShopJson != null) {
            return null;
        }
        //没有查询到去数据库查,getbyID默认的从数据库中查询
        /*  如果没有查到，缓存重建
            1 获取互斥锁
            2 获取所是否成功
            3 获取失败休眠
            4 获取成功根据id查询数据库
         */
        String lockKey = RedisConstants.LOCK_SHOP_KEY + id;
        Shop shop = null;
        try {
            boolean lock = tryLock(lockKey);
            if (!lock) {
                //获取互斥锁失败，休眠

                Thread.sleep(50);
                return queryByIdWithMutex(id);
            }
//            long s = System.currentTimeMillis();
            //获取锁成功检测一下redis中缓存是否存在，如果存在就不用重建缓存了
            String mutexShopJson = stringRedisTemplate.opsForValue().get(key);
            if (StrUtil.isNotBlank(mutexShopJson)) {
                shop = JSONUtil.toBean(mutexShopJson, Shop.class);
                return shop;
            }
            //判断是否为空值,之前缓存穿透设置的null
            if (mutexShopJson != null) {
                return null;
            }

            //重建缓存
            //从数据库中获取id 商户
            shop = getById(id);
            //模拟缓存重建过程
//            Thread.sleep(200);

            if (shop == null) {
                //解决缓存击穿
                //1将空值写入redis并限制时间
                stringRedisTemplate.opsForValue().set(key, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
                //2返回错误信息
                return null;
            }
            String jsonShop = JSONUtil.toJsonStr(shop);
            //写入redis缓存
            stringRedisTemplate.opsForValue().set(key, jsonShop, RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);
            long e = System.currentTimeMillis();
//            System.out.println("重建redis花费了:"+ (e-s));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            //释放互斥锁

            unLock(lockKey);
        }
        return shop;
    }

    //缓存穿透策略，设置null值
    /*
    public Shop queryByWithThoughPass(Long id) {
        //从redis中查询
        String key = RedisConstants.CACHE_SHOP_KEY+id;
        String redisShopJson = stringRedisTemplate.opsForValue().get(key);
        // NULL "" 都是Blank 有商铺数据才是true
        if (StrUtil.isNotBlank(redisShopJson)){
            Shop shop = JSONUtil.toBean(redisShopJson, Shop.class);
            return shop;
        }
        //判断是否为空值
        if (redisShopJson != null){
            return null;
        }
        //没有查询到去数据库查,getbyID默认的从数据库中查询
        Shop shop = getById(id);
        if (shop == null) {
            //解决缓存击穿
            //1将空值写入redis并限制时间
            stringRedisTemplate.opsForValue().set(key,"",RedisConstants.CACHE_NULL_TTL,TimeUnit.MINUTES);
            //2返回错误信息
            return null;
        }
        String jsonShop = JSONUtil.toJsonStr(shop);
        //写入redis缓存
        stringRedisTemplate.opsForValue().set(key,jsonShop,RedisConstants.CACHE_SHOP_TTL,TimeUnit.MINUTES);
        return shop;
    }
    */
    //添加缓存互斥锁
    public boolean tryLock(String key) {
        //完成一些重建要五百毫秒
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 500, TimeUnit.MILLISECONDS);
        return BooleanUtil.isTrue(flag);
    }

    //删除缓存互斥锁
    public void unLock(String key) {
        stringRedisTemplate.delete(key);
    }

    public void saveShop2Redis(Long id, Long expireTime) {
        Shop shop = getById(id);
        RedisData redisData = new RedisData();
        redisData.setData(shop);
        // expireTime是秒为单位（plusSeconds）
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(expireTime));
        stringRedisTemplate.opsForValue().set(RedisConstants.CACHE_SHOP_KEY + id, JSONUtil.toJsonStr(redisData));
    }

    @Override
    public Result update(Shop shop) {
        Long id = shop.getId();
        if (id == null)
            return Result.fail("店铺为空");
        updateById(shop);
        stringRedisTemplate.delete(RedisConstants.CACHE_SHOP_KEY + id);
        return Result.ok();
    }

    @Override
    public Result queryShopByType(Integer typeId, Integer current, Double x, Double y) {
        //1判断是否需要根据坐标查询
        if (x == null || y == null) {
            Page<Shop> page = query().eq("type_id", typeId)
                    .page(new Page<>(current, SystemConstants.DEFAULT_PAGE_SIZE));
            return Result.ok(page.getRecords());
        }
        //计算分页参数
        int from = (current - 1) * SystemConstants.DEFAULT_PAGE_SIZE;
        int end = current * SystemConstants.DEFAULT_PAGE_SIZE;
        //查询redis，按照距离排序分组，
        String key = RedisConstants.SHOP_GEO_KEY + typeId;
        GeoResults<RedisGeoCommands.GeoLocation<String>> results = stringRedisTemplate.opsForGeo()
                .radius(
                        key,
                        new Circle(new Point(x, y), new Distance(5000)),
                        RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs().includeCoordinates().limit(end)
                );

        //解析id
        if (results == null){
            return Result.ok(Collections.emptyList());
        }

        List<GeoResult<RedisGeoCommands.GeoLocation<String>>> list = results.getContent();
        if (list.size() <= from)
            return Result.ok(Collections.emptyList());
        //截取from - end的部分
        List<Long> ids = new ArrayList<>(list.size());
        Map<String, Distance> distanceMap = new HashMap<>(list.size());
        list.stream().skip(from).forEach(result -> {
            //获取店铺id
            String shopIdStr = result.getContent().getName();
            ids.add(Long.valueOf(shopIdStr));
            //获取距离
            Distance distance = result.getDistance();
            distanceMap.put(shopIdStr,distance);
        });
        //根据id查询shop
        String idStr = StrUtil.join(",", ids);
        List<Shop> shops = query().in("id", ids).last("ORDER BY FIELD(id," + idStr + ")").list();
        for (Shop shop : shops) {
            shop.setDistance(distanceMap.get(shop.getId().toString()).getValue());
        }
        return Result.ok(shops);

    }
}
