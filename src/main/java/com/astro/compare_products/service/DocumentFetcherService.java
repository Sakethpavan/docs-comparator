package com.astro.compare_products.service;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentFetcherService {

    private final MongoTemplate mongoTemplate;

    DocumentFetcherService(final MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public List<Document> fetchDocuments(String category, String collectionName) {
        Query query = new Query();
        query.addCriteria(Criteria.where("category").is(category));

        // Fetch the documents from the specified collection
        return mongoTemplate.find(query, Document.class, collectionName);
    }
}
