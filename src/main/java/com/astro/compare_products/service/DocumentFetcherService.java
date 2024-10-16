package com.astro.compare_products.service;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentFetcherService {

    @Autowired
    private MongoTemplate mongoTemplate;

    public List<Document> fetchDocuments(String category, String collectionName) {
        Query query = new Query();
        query.addCriteria(Criteria.where("category").is(category));

        // Fetch the documents from the specified collection
        return mongoTemplate.find(query, Document.class, collectionName);
    }
}
