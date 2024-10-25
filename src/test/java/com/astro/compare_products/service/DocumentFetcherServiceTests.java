package com.astro.compare_products.service;

import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the {@link DocumentFetcherService} class.
 * <p>
 * This test class verifies the functionality of {@link DocumentFetcherService},
 * specifically the ability to fetch documents from a MongoDB collection using
 * specified criteria.
 */
class DocumentFetcherServiceTests {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private DocumentFetcherService documentFetcherService;

    /**
     * Initializes mocks for each test case.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Tests that {@link DocumentFetcherService#fetchDocuments(String, String)} returns a list
     * of documents that match the specified category and collection name.
     * <p>
     * Expected behavior: A list of documents with the specified category is returned
     * when such documents exist in the collection.
     */
    @Test
    void testFetchDocuments_WithValidCategoryAndCollection_ReturnsDocuments() {
        // Arrange
        String category = "electronics";
        String collectionName = "products";

        Document document1 = new Document("category", category).append("name", "Product1");
        Document document2 = new Document("category", category).append("name", "Product2");
        List<Document> expectedDocuments = Arrays.asList(document1, document2);

        // Mock the MongoTemplate behavior
        when(mongoTemplate.find(any(Query.class), eq(Document.class), eq(collectionName)))
                .thenReturn(expectedDocuments);

        // Act
        List<Document> actualDocuments = documentFetcherService.fetchDocuments(category, collectionName);

        // Assert
        assertEquals(expectedDocuments, actualDocuments);
    }

    /**
     * Tests that {@link DocumentFetcherService#fetchDocuments(String, String)} returns an
     * empty list when no documents match the specified category.
     * <p>
     * Expected behavior: An empty list is returned when no documents in the collection
     * match the specified category.
     */
    @Test
    void testFetchDocuments_WithNoMatchingDocuments_ReturnsEmptyList() {
        // Arrange
        String category = "nonexistent";
        String collectionName = "products";

        // Mock the MongoTemplate behavior to return an empty list
        when(mongoTemplate.find(any(Query.class), eq(Document.class), eq(collectionName)))
                .thenReturn(List.of());

        // Act
        List<Document> actualDocuments = documentFetcherService.fetchDocuments(category, collectionName);

        // Assert
        assertEquals(0, actualDocuments.size());
    }

    /**
     * Verifies that the {@link DocumentFetcherService#fetchDocuments(String, String)} method
     * constructs a query with the correct criteria based on the specified category.
     * <p>
     * Expected behavior: The query passed to {@link MongoTemplate#find(Query, Class, String)}
     * matches the specified category criteria.
     */
    @Test
    void testFetchDocuments_VerifyQueryCriteria() {
        // Arrange
        String category = "clothing";
        String collectionName = "products";

        // Mock an empty result
        when(mongoTemplate.find(any(Query.class), eq(Document.class), eq(collectionName)))
                .thenReturn(List.of());

        // Act
        documentFetcherService.fetchDocuments(category, collectionName);

        // Assert (Verify that mongoTemplate.find was called with the expected query)
        Query expectedQuery = new Query();
        expectedQuery.addCriteria(Criteria.where("category").is(category));

        // verify to check if mongoTemplate.find was called with the expected query
        verify(mongoTemplate).find(expectedQuery, Document.class, collectionName);
    }
}
