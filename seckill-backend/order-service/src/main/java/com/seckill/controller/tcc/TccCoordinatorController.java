package com.seckill.controller.tcc;

import com.seckill.dto.tcc.SeckillTccSubmitRequest;
import com.seckill.dto.tcc.TccResponse;
import com.seckill.service.tcc.TccCoordinatorService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tcc/seckill")
public class TccCoordinatorController {

    private final TccCoordinatorService tccCoordinatorService;

    public TccCoordinatorController(TccCoordinatorService tccCoordinatorService) {
        this.tccCoordinatorService = tccCoordinatorService;
    }

    @PostMapping("/submit")
    public TccResponse submit(@RequestBody SeckillTccSubmitRequest request) {
        return tccCoordinatorService.submit(request);
    }
}


