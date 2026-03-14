package com.seckill.service.impl;

import org.springframework.dao.DuplicateKeyException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.seckill.entity.User;
import com.seckill.mapper.UserMapper;
import com.seckill.service.UserService;
import com.seckill.dto.LoginDTO;
import com.seckill.dto.RegisterDTO;
import com.seckill.vo.LoginVO;
import com.seckill.util.JwtUtil;
import com.seckill.util.PasswordEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j  // 添加这个注解
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

   @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO register(RegisterDTO registerDTO) {
        log.info("用户注册: {}", registerDTO.getUsername());
        
        // 1. 校验（逻辑层拦截）
        LambdaQueryWrapper<User> usernameWrapper = new LambdaQueryWrapper<>();
        usernameWrapper.eq(User::getUsername, registerDTO.getUsername());
        if (this.count(usernameWrapper) > 0) {
            throw new RuntimeException("用户名已被占用");
        }
        
        LambdaQueryWrapper<User> phoneWrapper = new LambdaQueryWrapper<>();
        phoneWrapper.eq(User::getPhone, registerDTO.getPhone());
        if (this.count(phoneWrapper) > 0) {
            throw new RuntimeException("手机号已被注册");
        }

        // 2. 组装数据
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setPhone(registerDTO.getPhone());
        user.setCreatedAt(LocalDateTime.now());
        
        // 3. 保存入库（捕获数据库级别的唯一约束异常，双重保险）
        try {
            this.save(user);
        } catch (DuplicateKeyException e) {
            log.error("并发注册拦截，手机号冲突: {}", registerDTO.getPhone());
            throw new RuntimeException("该手机号在极短时间内已被注册，请勿重复提交");
        }
        
        // 4. 自动生成Token实现注册后免密直接登录
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        
        LoginVO loginVO = new LoginVO();
        loginVO.setUserId(user.getId());
        loginVO.setUsername(user.getUsername());
        loginVO.setPhone(user.getPhone());
        loginVO.setToken(token);
        loginVO.setLoginTime(LocalDateTime.now());
        
        log.info("用户注册成功: {}", user.getId());
        return loginVO;
    }

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        log.info("用户登录: {}", loginDTO.getAccount());
        
        // 1. 查询用户（支持用户名或手机号登录）
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, loginDTO.getAccount())
               .or()
               .eq(User::getPhone, loginDTO.getAccount());
        
        User user = this.getOne(wrapper);
        
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 2. 校验密码
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        
        // 3. 生成JWT令牌
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        
        // 4. 返回登录信息
        LoginVO loginVO = new LoginVO();
        loginVO.setUserId(user.getId());
        loginVO.setUsername(user.getUsername());
        loginVO.setPhone(user.getPhone());
        loginVO.setToken(token);
        loginVO.setLoginTime(LocalDateTime.now());
        
        log.info("用户登录成功: {}", user.getId());
        return loginVO;
    }

    @Override
    public User getUserByPhone(String phone) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        return this.getOne(wrapper);
    }
}