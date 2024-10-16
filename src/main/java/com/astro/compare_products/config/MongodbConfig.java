package com.astro.compare_products.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;

@Component
@Configuration
public class MongodbConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongodbConfig.class);

    private final MongodbProperties mongodbProperties;

    public MongodbConfig(final MongodbProperties mongodbProperties) {
        this.mongodbProperties = mongodbProperties;
    }

    @Bean
    public MongoClient mongoClient() {
        var connectionString = MessageFormat.format(
                "mongodb+srv://{0}:{1}@{2}/{3}?authSource=admin",
                mongodbProperties.getUserName(),
                mongodbProperties.getPassword(),
                mongodbProperties.getUri(),
                mongodbProperties.getDatabase()
        );
        return MongoClients.create(connectionString);
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoClient(), mongodbProperties.getDatabase());
    }

    @Bean
    public MongoDatabaseFactory mongoDatabaseFactory(MongoClient mongoClient) {
        return new SimpleMongoClientDatabaseFactory(mongoClient, mongodbProperties.getDatabase());
    }

    @Bean
    public MongoTransactionManager transactionManager(MongoDatabaseFactory mongoDatabaseFactory) {
        return new MongoTransactionManager(mongoDatabaseFactory);
    }
}
