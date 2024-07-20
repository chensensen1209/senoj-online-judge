package com.sen.senojbackendvoucherservice.utils;

import cn.hutool.core.bean.BeanUtil;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import org.apache.ibatis.plugin.Interceptor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @ClassDescription:
 * @Author: chensen
 * @Created: 2024/7/6 14:45
 */
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        System.out.println("=========进入LoginInterceptor=========");
        //判断是否拦截 （threadlocal中是否有用户）
        if (UserHolder.getUser() == null){
            response.setStatus(401);
            System.out.println("=========UserHolder.getUser() == null离开LoginInterceptor=========");
            //拦截
            return false;
        }
        //有用户放行
        System.out.println("=========离开LoginInterceptor=========");
        return true;
    }

}
