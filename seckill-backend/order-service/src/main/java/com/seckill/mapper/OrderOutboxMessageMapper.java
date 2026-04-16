package com.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seckill.entity.OrderOutboxMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderOutboxMessageMapper extends BaseMapper<OrderOutboxMessage> {
}


