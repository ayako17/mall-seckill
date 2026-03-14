package com.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.seckill.entity.User;
import com.seckill.dto.LoginDTO;
import com.seckill.dto.RegisterDTO;
import com.seckill.vo.LoginVO;

public interface UserService extends IService<User> {
    
    /**
     * 用户注册
     * @param registerDTO 注册信息
     * @return 登录信息
     */
    LoginVO register(RegisterDTO registerDTO);
    
    /**
     * 用户登录
     * @param loginDTO 登录信息
     * @return 登录信息
     */
    LoginVO login(LoginDTO loginDTO);
    
    /**
     * 根据手机号查询用户
     * @param phone 手机号
     * @return 用户信息
     */
    User getUserByPhone(String phone);
}