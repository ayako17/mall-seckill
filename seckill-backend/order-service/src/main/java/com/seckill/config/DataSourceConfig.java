package com.seckill.config;

import org.springframework.context.annotation.Configuration;

/**
 * Order service relies on ShardingSphere-JDBC auto-configuration.
 * Keep this class to avoid re-introducing conflicting manual DataSource beans.
 */
@Configuration
public class DataSourceConfig {
}
