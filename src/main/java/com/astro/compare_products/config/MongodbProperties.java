package com.astro.compare_products.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "spring.data.mongodb")
@Data
public class MongodbProperties {
    private String userName;
    private String password;
    private String uri;
    private String database;
}
