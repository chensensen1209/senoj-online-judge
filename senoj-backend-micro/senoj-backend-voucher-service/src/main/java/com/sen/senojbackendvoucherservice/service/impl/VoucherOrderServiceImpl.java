package com.sen.senojbackendvoucherservice.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.SeckillVoucher;
import com.hmdp.entity.VoucherOrder;
import com.hmdp.mapper.VoucherOrderMapper;
import com.hmdp.service.ISeckillVoucherService;
import com.hmdp.service.IVoucherOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.Impl.SimpleRedisLock;
import com.hmdp.utils.RedisIdWorker;
import com.hmdp.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
@Slf4j
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {
    @Resource
    private ISeckillVoucherService seckillVoucherService;

    @Resource
    @Lazy
    private IVoucherOrderService voucherOrderService;

    @Resource
    private RedisIdWorker redisIdWorker;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedissonClient redissonClient;

    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;

    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    private static final ExecutorService SECKILL_ORDER_EXECUTOR = Executors.newSingleThreadExecutor();

    @PostConstruct
    private void init() {
        SECKILL_ORDER_EXECUTOR.submit(() -> {
            String queueName = "stream.orders";
            while (true) {
                try {
                    //从消息队列中获取订单信息
                    List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                            Consumer.from("g1", "c1")
                            , StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2))
                            , StreamOffset.create(queueName, ReadOffset.lastConsumed())
                    );
                    //判断消息时候获取成功
                    if (list == null || list.isEmpty()) {
                        //获取失败 没有消息 继续循环
                        continue;
                    }
                    //获取成功 解析消息
                    MapRecord<String, Object, Object> record = list.get(0);
                    Map<Object, Object> values = record.getValue();
                    VoucherOrder voucherOrder = BeanUtil.fillBeanWithMap(values, new VoucherOrder(), true);
                    //下单
                    voucherOrderService.createVoucherOrder(voucherOrder);
                    //ack确认消息
                    stringRedisTemplate.opsForStream().acknowledge(queueName, "g1", record.getId());
                } catch (Exception e) {
                    e.printStackTrace();
                    handlePendingList();
                }
            }
        });
    }

    private void handlePendingList() {
        String queueName = "stream.orders";
        while (true) {
            try {
                //1获取pending-list的订单信息,获取pending-list中的信息
                List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                        Consumer.from("g1", "c1"),
                        StreamReadOptions.empty().count(1),
                        StreamOffset.create(queueName, ReadOffset.from("0"))
                );
                //判断是否获取成功
                if (list == null || list.isEmpty()) {
                    //获取失败，没有异常消息了，结束循环
                    break;
                }
                //获取成功下单
                MapRecord<String, Object, Object> record = list.get(0);
                Map<Object, Object> values = record.getValue();
                VoucherOrder voucherOrder = BeanUtil.fillBeanWithMap(values, new VoucherOrder(), true);
                handlerVoucherOrder(voucherOrder);
                //ack确认
                stringRedisTemplate.opsForStream().acknowledge(queueName, "g1", record.getId());
            } catch (Exception e) {
                log.error("订单pendinglist处理异常", e);
                try {
                    Thread.sleep(20);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    /**
     * @param voucherOrder: 处理订单
     * @return void
     * @author xh
     * @description TODO
     * @date 2024/7/11 16:02
     */
    private void handlerVoucherOrder(VoucherOrder voucherOrder) {
        voucherOrderService.createVoucherOrder(voucherOrder);
    }



    /**
     * @param voucherId: 优惠券id
     * @return Result
     * @author xh
     * @description 使用队列的，新增秒杀优惠券订单,
     * @date 2024/7/8 22:12
     */
    @Override
    public Result secKillVoucher(Long voucherId) {
        Long userId = UserHolder.getUser().getId();
        long orderId = redisIdWorker.nextId("order");
        // 1、执行lua脚本
        Long result = stringRedisTemplate.execute(
                SECKILL_SCRIPT,
                Collections.emptyList(),
                voucherId.toString(),
                userId.toString(),
                String.valueOf(orderId)
        );
        int r = result.intValue();
        // 2 判断结果为0
        if (r != 0) {
            // 2.1不为0 没有购买资格
            return Result.fail(r == 1 ? "库存不足" : "不能重复下单");
        }
        // 创建代理对象(事务)
        // proxy = (IVoucherOrderService) AopContext.currentProxy();
        // 3 返回订单id
        return Result.ok(orderId);
    }

    /**
     * @param voucherOrder: 优惠券秒杀订单
     * @return Result
     * @author xh
     * @description 创建订单加锁，如果锁在该方法内部，那么存在并发安全问题
     * 例如，锁已经结束，但事务还无提交，此时仍然那会导致不一致问题
     * @date 2024/7/9 19:33
     */
    @Transactional
    public void createVoucherOrder(VoucherOrder voucherOrder) {
        //3.5一人一单
        Long userId = voucherOrder.getUserId();
        // 查询订单数量（eq.userid and eq.voucherID）
        int count = query().eq("user_id", userId).eq("voucher_id", voucherOrder.getVoucherId()).count();
        // 判断用户的订单数量是否大于0
        if (count > 0) {
            // 如果存在订单，那么失败
            log.error(userId + "您已经购买了");
            return;
        }
        //4扣减库存
        // todo 加锁
        boolean success = seckillVoucherService.update().setSql("stock = stock -  1")
                .eq("voucher_id", voucherOrder.getVoucherId())
                .gt("stock", 0)
                .update();
        if (!success) {
            log.error("库存不足");
            return;
        }
        voucherOrderService.save(voucherOrder);
    }
}
