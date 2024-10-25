package com.astro.compare_products.service;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for fetching documents from a MongoDB collection based on specified criteria.
 * <pr>
 * This service utilizes {@link MongoTemplate} to interact with the database, allowing
 * it to fetch documents matching a given category from a specified collection.
 */
@Service
public class DocumentFetcherService {

    private final MongoTemplate mongoTemplate;

    /**
     * Constructs a new instance of {@link DocumentFetcherService} with the given {@link MongoTemplate}.
     *
     * @param mongoTemplate the MongoTemplate used for database interactions
     */
    DocumentFetcherService(final MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Fetches documents from the specified collection that match the provided category.
     *
     * @param category the category to filter documents by
     * @param collectionName the name of the MongoDB collection to fetch documents from
     * @return a list of documents matching the specified category in the provided collection
     */
    public List<Document> fetchDocuments(String category, String collectionName) {
        Query query = new Query();
        query.addCriteria(Criteria.where("category").is(category));

        // Fetch the documents from the specified collection
        return mongoTemplate.find(query, Document.class, collectionName);
    }
}
