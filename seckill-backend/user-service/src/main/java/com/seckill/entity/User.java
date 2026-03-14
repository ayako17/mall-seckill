package com.seckill.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户基础信息实体类
 */
@Data // 自动生成 getters, setters, toString, equals, hashCode
@TableName("user")
public class User {
    
    @TableId(type = IdType.ASSIGN_ID) // 雪花算法生成全局唯一ID
    private Long id;
    
    private String username;
    
    private String password;
    
    private String phone;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}