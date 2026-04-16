package com.seckill.service;

import com.seckill.dto.tcc.TccOrderRequest;
import com.seckill.dto.tcc.TccResponse;

public interface InventoryTccService {

    TccResponse tryReserve(TccOrderRequest request);

    TccResponse confirm(String xid);

    TccResponse cancel(String xid);
}


