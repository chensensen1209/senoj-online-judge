package com.sen.senojbackendvoucherservice.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.TimeoutUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private IUserService userService;

    @Override
    public Result sendCode(String phone, HttpSession session) {
        // 1校验手机号
        boolean phoneInvalid = RegexUtils.isPhoneInvalid(phone);
        //2不符合 返回错误信息
        if (phoneInvalid)
            return Result.fail("手机号格式错误");
        // 3 符合 生成验证码
        String code = RandomUtil.randomNumbers(6);
        // 4 保存验证码到session
//        session.setAttribute("code",code);
        // 保存验证码到redis,并设置时间 set key value ex:有效期
        stringRedisTemplate.opsForValue().set(RedisConstants.LOGIN_CODE_KEY + phone, code, RedisConstants.LOGIN_CODE_TTL, TimeUnit.MINUTES);
        // 5 发送验证码 ，
        // todo 需要第三方，后续加入
        log.debug("发送验证码成功，验证码{}", code);
        // 6 返回状态
        return Result.ok();

    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        String phone = loginForm.getPhone();
        // 校验手机号
        boolean phoneInvalid = RegexUtils.isPhoneInvalid(phone);
        if (phoneInvalid)
            return Result.fail("手机号格式错误");
        // 校验验证码 从session获取
//        String sessionCode = (String) session.getAttribute("code");
//        String loginCode = loginForm.getCode();
        // 从redis获取
        String redisCode = stringRedisTemplate.opsForValue().get(RedisConstants.LOGIN_CODE_KEY + phone);
        String loginCode = loginForm.getCode();
        if (redisCode == null || !loginCode.equals(redisCode)) {
            return Result.fail("验证码错误");
        }
        // 根据手机号查询用户
        User user = query().eq("phone", phone).one();

        // 如果用户为空
        if (user == null) {
            user = createNewUser(phone);
        }
        // 保存用户信息到session
        // session.setAttribute("user", BeanUtil.copyProperties(user, UserDTO.class));
        // 保存用户信息到redis
        // 1随机生成token作为登录令牌
        String token = UUID.randomUUID().toString();
        // 将user对象转为UserDTO
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        //转换为map形式 并存储
        //CopyOptions.create()后面是因为，
        // 使用的redis工具类是str - str的，但是user中包含有long类型，下面就是对long类型就系转换
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(), CopyOptions.create().
                setIgnoreNullValue(true).
                setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));
        // 、返回token
        stringRedisTemplate.opsForHash().putAll(RedisConstants.LOGIN_USER_KEY + token, userMap);
        // 设置token有效期
        String tokenKey = RedisConstants.LOGIN_USER_KEY + token;
        stringRedisTemplate.expire(tokenKey, RedisConstants.LOGIN_USER_TTL, TimeUnit.MINUTES);
        return Result.ok(token);
    }

    @Override
    public Result queryUserById(Long userId) {
        User user = userService.getById(userId);
        if (user == null) {
            return Result.ok();
        }
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        // 返回
        return Result.ok(userDTO);
    }

    @Override
    public Result sign() {
        //获取当前登录用户
        Long userId = UserHolder.getUser().getId();
        //2获取日期
        LocalDateTime now = LocalDateTime.now();
        //拼接key
        String dataFormat = now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        String key = RedisConstants.USER_SIGN_KEY + userId + dataFormat;
        //获取几天是本月第几天
        int dayOfMonth = now.getDayOfMonth();
        //写入redis
        stringRedisTemplate.opsForValue().setBit(key,dayOfMonth - 1,true);
        return Result.ok();
    }

    @Override
    public Result signCount() {
        //获取用户本月的所有签到记录
        //获取当前登录用户
        Long userId = UserHolder.getUser().getId();
        //2获取日期
        LocalDateTime now = LocalDateTime.now();
        //拼接key
        String dataFormat = now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        String key = RedisConstants.USER_SIGN_KEY + userId + dataFormat;
        //获取几天是本月第几天
        int dayOfMonth = now.getDayOfMonth();
        //获取本月介质的∀签到记录
        List<Long> result = stringRedisTemplate.opsForValue().bitField(
                key, BitFieldSubCommands.create().
                        get(BitFieldSubCommands.BitFieldType.unsigned(dayOfMonth)).valueAt(0)
        );
        if (result == null || result.isEmpty()){
            return Result.ok(0);
        }
        Long num = result.get(0);
        if (num == null || num ==0)
            return Result.ok(0);
        //循环遍历
        int count = 0;
        while (true){
            if ((num & 1)==0){
                break;
            }else {
                count++;
            }
        }
        num >>>= 1;
        return Result.ok(count);



    }

    private User createNewUser(String phone) {
        // 创建用户
        String userPhone = phone;
        String userName = SystemConstants.USER_NICK_NAME_PREFIX + RandomUtil.randomString(10);
        User user = new User();
        user.setPhone(userPhone);
        user.setNickName(userName);
        //保存新用户到数据库
        save(user);
        return user;
    }
}
