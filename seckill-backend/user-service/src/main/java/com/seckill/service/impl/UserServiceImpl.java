package com.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.seckill.entity.User;
import com.seckill.mapper.UserMapper;
import com.seckill.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import java.time.LocalDateTime;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public String register(User user) {
        // 1. 校验用户名是否已存在
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", user.getUsername());
        if (this.count(wrapper) > 0) {
            return "注册失败：用户名已被占用！";
        }

        // 2. 密码 MD5 加密 (商业项目绝对不能明文存密码)
        String md5Password = DigestUtils.md5DigestAsHex(user.getPassword().getBytes());
        user.setPassword(md5Password);
        
        // 3. 设置创建时间并保存到阿里云数据库
        user.setCreateTime(LocalDateTime.now());
        this.save(user);
        
        return "注册成功！欢迎您，" + user.getUsername();
    }

    @Override
    public String login(String username, String password) {
        // 1. 根据用户名查询用户
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);
        User user = this.getOne(wrapper);

        if (user == null) {
            return "登录失败：用户不存在！";
        }

        // 2. 校验密码 (将用户输入的密码MD5加密后，与数据库中的密文比对)
        String md5Password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!md5Password.equals(user.getPassword())) {
            return "登录失败：密码错误！";
        }

        // TODO: 实际秒杀项目中，这里通常会生成并返回一个 JWT Token，后续请求带着 Token 访问。
        return "登录成功！当前处理线程: " + Thread.currentThread().toString();
    }
}