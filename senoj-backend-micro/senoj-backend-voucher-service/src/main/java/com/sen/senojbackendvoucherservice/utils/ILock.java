package com.sen.senojbackendvoucherservice.utils;

/**
 * @ClassDescription: redis分布式锁接口
 * @Author: chensen
 * @Created: 2024/7/9 20:42
 */
public interface ILock {
    /**
     * @param timeoutSec: 分布式锁的到期时间，过期自动释放
     * @return boolean
     * @author xh
     * @description TODO
     * @date 2024/7/9 20:43
     */

    boolean tryLock(long timeoutSec);
    /**
     * @param :
     * @return void
     * @author xh
     * @description 释放锁
     * @date 2024/7/9 20:44
     */

    void unLock();
}
