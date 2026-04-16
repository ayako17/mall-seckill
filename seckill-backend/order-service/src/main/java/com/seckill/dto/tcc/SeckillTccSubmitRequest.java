package com.seckill.dto.tcc;

import lombok.Data;

@Data
public class SeckillTccSubmitRequest {

    private Long userId;

    private Long seckillId;

    private Integer quantity;
}


