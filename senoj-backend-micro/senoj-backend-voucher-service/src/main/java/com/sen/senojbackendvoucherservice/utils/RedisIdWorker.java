package com.sen.senojbackendvoucherservice.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * @ClassDescription:
 * @Author: chensen
 * @Created: 2024/7/7 21:01
 */
@Component
public class RedisIdWorker {
    private StringRedisTemplate stringRedisTemplate;

    public RedisIdWorker(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    //起始时间 2024/1/1/0/0开始
    private static final long BEGIN_TIME = 1704067200L;
    //序列号长度32位
    private static final int COUNT_BITS = 32;

    /**
     * @param keyPrefix:
     * @return long
     * @author xh
     * @description 生成redis中的id，id 第一位符号位 31为时间戳 32序列号
     * @date 2024/7/7 21:12
     */
    public long nextId(String keyPrefix){
        //生成时间戳
        LocalDateTime now = LocalDateTime.now();
        long nowSeconds = now.toEpochSecond(ZoneOffset.UTC);
        long timeStamp = nowSeconds - BEGIN_TIME;
        //生成序列号
        // 1获取当天日期
        String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        // 2自增长
        long count = stringRedisTemplate.opsForValue().increment("icr:" + keyPrefix + ":" + date);
        // 拼接返回
        return timeStamp << COUNT_BITS | count;
    }


}
