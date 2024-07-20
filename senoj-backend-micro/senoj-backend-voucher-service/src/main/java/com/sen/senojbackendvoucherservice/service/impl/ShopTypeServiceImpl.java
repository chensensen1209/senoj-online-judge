package com.sen.senojbackendvoucherservice.service.impl;

import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public Result queryShopTypeAll() {
        // 从Redis中获取商家类型列表
        List<String> shopTypeListInRedis = stringRedisTemplate.opsForList().range(RedisConstants.SHOP_TYPE_KEY, 0, -1);

        // 非空判断
        if (shopTypeListInRedis != null && !shopTypeListInRedis.isEmpty()) {
            // 将JSON字符串转换为ShopType对象
            List<ShopType> shopTypeList = shopTypeListInRedis.stream()
                    .map(item -> JSONUtil.toBean(item, ShopType.class))
                    .collect(Collectors.toList());
            return Result.ok(shopTypeList);
        }

        // 从数据库中查询商家类型列表
        List<ShopType> shopTypeList = query().orderByAsc("sort").list();
        if (shopTypeList.isEmpty()) {
            return Result.fail("未查到商家类型列表");
        }

        // 将查询结果转换为JSON字符串并存储到Redis中
        List<String> redisShopType = shopTypeList.stream()
                .map(item -> JSONUtil.toJsonStr(item))
                .collect(Collectors.toList());
        stringRedisTemplate.opsForList().rightPushAll(RedisConstants.SHOP_TYPE_KEY, redisShopType);
        //一般不会改变的不设置时间
        //stringRedisTemplate.expire(RedisConstants.SHOP_TYPE_KEY, 30, TimeUnit.MINUTES);

        return Result.ok(shopTypeList);
    }

}
