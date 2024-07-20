package com.sen.senojbackendvoucherservice.utils.Impl;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.BooleanUtil;
import com.hmdp.utils.ILock;
import com.sun.org.apache.bcel.internal.generic.NEW;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * @ClassDescription:
 * @Author: chensen
 * @Created: 2024/7/9 20:44
 */
public class SimpleRedisLock implements ILock {

    //业务名称，不同业务有不同的锁
    private String name;
    private StringRedisTemplate stringRedisTemplate;

    public SimpleRedisLock(String name, StringRedisTemplate stringRedisTemplate) {
        this.name = name;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    private static final String KEY_PREFIX = "lock:";
    private static final String ID_PREFIX = UUID.randomUUID().toString(true) + "-";

    //预加载脚本
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT;
    static {
        UNLOCK_SCRIPT = new DefaultRedisScript<>();
        UNLOCK_SCRIPT.setLocation(new ClassPathResource("unlock.lua"));
        UNLOCK_SCRIPT.setResultType(Long.class);
    }
    @Override
    public boolean tryLock(long timeoutSec) {
        // 获取当前线程表示，线程独有的一个key，集群也一样
        String threadId = ID_PREFIX + Thread.currentThread().getId();
        // 获取锁
        // 添加锁 set lock thread1 nx ex 10 ,
        // thread1专享一个锁，别人都知道thread1在用
        // KEY_PREFIX + name 业务名
        Boolean success =
                stringRedisTemplate.opsForValue().setIfAbsent(KEY_PREFIX + name, threadId, timeoutSec, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(success);
    }

    @Override
    public void unLock() {
        //判断表示是否一致
        /*
        String threadId = ID_PREFIX + Thread.currentThread().getId();
        String id = stringRedisTemplate.opsForValue().get(KEY_PREFIX + name);
        if (id.equals(threadId)) {
            //存在原子性问题
            stringRedisTemplate.delete(KEY_PREFIX + name);
         }
         */
        //执行lua脚本
        stringRedisTemplate.execute(
                UNLOCK_SCRIPT,
                Collections.singletonList(KEY_PREFIX + name),
                ID_PREFIX + Thread.currentThread().getId());
    }
}
