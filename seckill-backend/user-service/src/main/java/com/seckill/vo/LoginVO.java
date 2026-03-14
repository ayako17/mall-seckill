package com.seckill.vo;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

/**
 * 登录响应VO
 */
@Data
public class LoginVO {
    
    private Long userId;           // 用户ID
    private String username;       // 用户名
    private String phone;          // 手机号
    private String token;          // JWT令牌
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime loginTime; // 登录时间
}