package com.sen.senojbackendvoucherservice.service;

import com.hmdp.dto.Result;
import com.hmdp.entity.VoucherOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sun.istack.internal.NotNull;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IVoucherOrderService extends IService<VoucherOrder> {

    /**
     * @param voucherId: 优惠券id
     * @return Result
     * @author xh
     * @description 秒杀优惠券
     * @date 2024/7/11 16:18
     */
    Result secKillVoucher(Long voucherId);

    /**
     * @param voucherId:优惠券id
     * @return void
     * @author xh
     * @description 生成优惠券订单
     * @date 2024/7/11 16:18
     */
    @NotNull
    @Transactional(rollbackFor = Exception.class)
    void createVoucherOrder(VoucherOrder voucherId);
}
