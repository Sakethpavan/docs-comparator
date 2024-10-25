package com.astro.compare_products.service;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Service for fetching documents from a MongoDB collection based on specified criteria.
 * <p>
 * This service utilizes {@link MongoTemplate} to interact with the MongoDB database,
 * allowing it to fetch documents that match various specified criteria from a given collection.
 * The criteria are passed as a key-value pair map where the keys represent the field names
 * and the values represent the corresponding field values to filter by.
 * </p>
 */
@Service
public class DocumentFetcherService {

    private final MongoTemplate mongoTemplate;

    /**
     * Constructs a new instance of {@link DocumentFetcherService} with the provided {@link MongoTemplate}.
     *
     * @param mongoTemplate the MongoTemplate used for database interactions, enabling this service to execute queries.
     */
    public DocumentFetcherService(final MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Fetches documents from the specified collection that match the provided criteria.
     *
     * @param collectionName the name of the MongoDB collection to fetch documents from.
     * @param criteria a map containing the criteria to filter documents, where keys are field names
     *                 and values are the values to match against those fields.
     * @return a list of documents matching the specified criteria in the provided collection.
     *         Returns an empty list if no documents match the criteria.
     */
    public List<Document> fetchDocuments(String collectionName, Map<String, String> criteria) {
        Query query = new Query();
        criteria.forEach((key, value) -> query.addCriteria(Criteria.where(key).is(value)));
        // Fetch the documents from the specified collection based on the constructed query
        return mongoTemplate.find(query, Document.class, collectionName);
    }
}
