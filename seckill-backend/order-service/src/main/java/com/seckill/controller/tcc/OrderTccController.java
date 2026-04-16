package com.seckill.controller.tcc;

import com.seckill.dto.tcc.TccOrderRequest;
import com.seckill.dto.tcc.TccResponse;
import com.seckill.service.tcc.OrderTccService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tcc/order")
public class OrderTccController {

    private final OrderTccService orderTccService;

    public OrderTccController(OrderTccService orderTccService) {
        this.orderTccService = orderTccService;
    }

    @PostMapping("/try")
    public TccResponse tryReserve(@RequestBody TccOrderRequest request) {
        return orderTccService.tryReserve(request);
    }

    @PostMapping("/confirm/{xid}")
    public TccResponse confirm(@PathVariable String xid) {
        return orderTccService.confirm(xid);
    }

    @PostMapping("/cancel/{xid}")
    public TccResponse cancel(@PathVariable String xid) {
        return orderTccService.cancel(xid);
    }
}


