package com.seckill.dto.payment;

import lombok.Data;

@Data
public class PaymentCallbackRequest {

    private String orderNo;

    private Integer paymentMethod;

    private String transactionId;
}


