package com.seckill.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("order_outbox_message")
public class OrderOutboxMessage {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String msgKey;

    private String topic;

    private String payload;

    private Integer status;

    private Integer retryCount;

    private LocalDateTime nextRetryTime;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;
}


