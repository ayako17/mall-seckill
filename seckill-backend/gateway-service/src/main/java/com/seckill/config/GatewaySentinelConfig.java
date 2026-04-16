package com.seckill.config;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class GatewaySentinelConfig {

    @PostConstruct
    public void initBlockHandler() {
        BlockRequestHandler handler = (exchange, throwable) -> {
            Map<String, Object> body = new HashMap<>();
            body.put("code", 429);
            body.put("message", "Too many requests, please retry later");
            body.put("route", exchange.getRequest().getPath().value());
            return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body);
        };
        GatewayCallbackManager.setBlockHandler(handler);
    }
}