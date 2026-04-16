package com.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seckill.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    @Select("SELECT * FROM orders WHERE order_no = #{orderNo} LIMIT 1")
    Order selectByOrderNo(@Param("orderNo") String orderNo);

    @Update("UPDATE orders SET status = 1, payment_method = #{paymentMethod}, transaction_id = #{transactionId}, pay_time = #{payTime} " +
            "WHERE order_no = #{orderNo} AND status = 0")
    int markOrderPaid(@Param("orderNo") String orderNo,
                      @Param("paymentMethod") Integer paymentMethod,
                      @Param("transactionId") String transactionId,
                      @Param("payTime") LocalDateTime payTime);

    @Update("UPDATE orders SET status = 6 WHERE order_no = #{orderNo} AND status = 0")
    int markReserveFailed(@Param("orderNo") String orderNo);
}


