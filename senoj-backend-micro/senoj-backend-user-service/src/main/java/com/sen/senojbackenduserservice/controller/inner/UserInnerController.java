package com.sen.senojbackenduserservice.controller.inner;

import com.sen.senojbackendmodel.entity.User;
import com.sen.senojbackendserviceclient.service.UserFeignClient;
import com.sen.senojbackenduserservice.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

/**
 * @ClassDescription:
 * @Author: chensen
 * @Created: 2024/5/27 16:43
 */
@RestController
@RequestMapping("/inner")
public class UserInnerController implements UserFeignClient {

    @Resource
    private UserService userService;
    /**
     * @param userId:
     * @return User
     * @author xh
     * @description 根据用户获取id
     * @date 2024/5/27 15:44
     */
    @Override
    @GetMapping("/get/id")
    public User getById(@RequestParam("userId") long userId){
        return userService.getById(userId);
    }
    /**
     * @param idList:
     * @return List<User>
     * @author xh
     * @description 根据id获取用户列表
     * @date 2024/5/27 15:45
     */
    @Override
    @GetMapping("/get/ids")
    public List<User> listByIds(@RequestParam("idList") Collection<Long> idList){
        return userService.listByIds(idList);
    }

}
