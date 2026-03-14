package com.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.seckill.entity.User;

public interface UserService extends IService<User> {
    // 注册方法
    String register(User user);
    // 登录方法
    String login(String username, String password);
}