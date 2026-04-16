package com.seckill.service.tcc;

import com.seckill.client.InventoryTccFeignClient;
import com.seckill.dto.tcc.SeckillTccSubmitRequest;
import com.seckill.dto.tcc.TccOrderRequest;
import com.seckill.dto.tcc.TccResponse;
import com.seckill.entity.SeckillProduct;
import com.seckill.mapper.SeckillProductMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class TccCoordinatorService {

    private final OrderTccService orderTccService;
    private final SeckillProductMapper seckillProductMapper;
    private final InventoryTccFeignClient inventoryTccFeignClient;

    public TccCoordinatorService(OrderTccService orderTccService,
                                 SeckillProductMapper seckillProductMapper,
                                 InventoryTccFeignClient inventoryTccFeignClient) {
        this.orderTccService = orderTccService;
        this.seckillProductMapper = seckillProductMapper;
        this.inventoryTccFeignClient = inventoryTccFeignClient;
    }

    public TccResponse submit(SeckillTccSubmitRequest request) {
        String xid = UUID.randomUUID().toString();
        int quantity = request.getQuantity() == null ? 1 : request.getQuantity();

        SeckillProduct product = seckillProductMapper.selectById(request.getSeckillId());
        if (product == null) {
            return TccResponse.fail("seckill product not found");
        }

        TccOrderRequest orderReq = new TccOrderRequest();
        orderReq.setXid(xid);
        orderReq.setUserId(request.getUserId());
        orderReq.setProductId(product.getProductId());
        orderReq.setSeckillId(request.getSeckillId());
        orderReq.setQuantity(quantity);
        orderReq.setPayAmount(product.getSeckillPrice().multiply(BigDecimal.valueOf(quantity)));

        boolean inventoryTryOk = false;
        boolean orderTryOk = false;

        try {
            TccResponse inventoryTry = inventoryTccFeignClient.tryReserve(orderReq);
            inventoryTryOk = inventoryTry != null && inventoryTry.isSuccess();
            if (!inventoryTryOk) {
                return TccResponse.fail("inventory try failed");
            }

            TccResponse orderTry = orderTccService.tryReserve(orderReq);
            orderTryOk = orderTry.isSuccess();
            if (!orderTryOk) {
                throw new IllegalStateException("order try failed");
            }

            orderTccService.confirm(xid);
            TccResponse inventoryConfirm = inventoryTccFeignClient.confirm(xid);
            if (inventoryConfirm == null || !inventoryConfirm.isSuccess()) {
                throw new IllegalStateException("inventory confirm failed");
            }
            return TccResponse.ok("global tcc success");
        } catch (Exception ex) {
            if (orderTryOk) {
                orderTccService.cancel(xid);
            }
            if (inventoryTryOk) {
                try {
                    inventoryTccFeignClient.cancel(xid);
                } catch (Exception ignore) {
                    // keep best effort cancel in example implementation
                }
            }
            return TccResponse.fail("global tcc failed: " + ex.getMessage());
        }
    }
}