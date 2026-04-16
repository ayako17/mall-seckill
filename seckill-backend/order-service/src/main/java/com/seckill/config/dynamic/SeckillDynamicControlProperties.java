package com.seckill.config.dynamic;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@RefreshScope
@Component
@ConfigurationProperties(prefix = "seckill.control")
public class SeckillDynamicControlProperties {

    private boolean enabled = true;

    private int requestThresholdPerSecond = 300;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getRequestThresholdPerSecond() {
        return requestThresholdPerSecond;
    }

    public void setRequestThresholdPerSecond(int requestThresholdPerSecond) {
        this.requestThresholdPerSecond = requestThresholdPerSecond;
    }
}