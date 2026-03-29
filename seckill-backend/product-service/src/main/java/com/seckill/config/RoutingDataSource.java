package com.seckill.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class RoutingDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        String key = DataSourceContextHolder.get();
        // 未设置时默认为 master（安全网）
        return key != null ? key : DataSourceContextHolder.MASTER;
    }
}