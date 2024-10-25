package com.astro.compare_products.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.bson.Document;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the {@link DocumentComparisonService} class.
 * <p>
 * This class verifies the functionality of DocumentComparisonService, including
 * document comparisons, handling of nested documents, and ignoring specified fields.
 */

@ExtendWith(MockitoExtension.class)
class DocumentComparisonServiceTests {

    @InjectMocks
    private DocumentComparisonService documentComparisonService;

    /**
     * Sets up necessary properties before each test, specifically for ignored fields and key fields.
     */
    @BeforeEach
    void setUp() {
        // Set mock values for key fields and ignored fields
        ReflectionTestUtils.setField(documentComparisonService, "ignoredFieldsProperty", "fieldToIgnore");
        ReflectionTestUtils.setField(documentComparisonService, "keyFieldsProperty", "keyField1,keyField2");

        // Initialize key fields
        documentComparisonService.init();
    }

    /**
     * Tests that {@link DocumentComparisonService#compareDocuments(List, List)}
     * correctly identifies documents that are unique to the first collection.
     */
    @Test
    void testCompareDocuments_DocsInFirstOnly() {
        // Arrange
        Document doc1 = new Document("keyField1", "A").append("field", "value1");
        List<Document> collection1Docs = Collections.singletonList(doc1);
        List<Document> collection2Docs = Collections.emptyList();

        // Act
        Map<String, Object> result = documentComparisonService.compareDocuments(collection1Docs, collection2Docs);

        // Assert
        List<Document> docsInFirstOnly = (List<Document>) result.get("docsInFirstOnly");
        assertEquals(1, docsInFirstOnly.size());
        assertEquals(doc1, docsInFirstOnly.getFirst());
    }

    /**
     * Tests that {@link DocumentComparisonService#compareDocuments(List, List)}
     * correctly identifies documents unique to the second collection.
     */
    @Test
    void testCompareDocuments_DocsInSecondOnly() {
        // Arrange
        Document doc2 = new Document("keyField1", "B").append("field", "value2");
        List<Document> collection1Docs = Collections.emptyList();
        List<Document> collection2Docs = Collections.singletonList(doc2);

        // Act
        Map<String, Object> result = documentComparisonService.compareDocuments(collection1Docs, collection2Docs);

        // Assert
        List<Document> docsInSecondOnly = (List<Document>) result.get("docsInSecondOnly");
        assertEquals(1, docsInSecondOnly.size());
        assertEquals(doc2, docsInSecondOnly.getFirst());
    }

    /**
     * Tests that {@link DocumentComparisonService#compareDocuments(List, List)}
     * correctly identifies differences in matching documents between the two collections.
     */
    @Test
    void testCompareDocuments_DifferingDocs() {
        // Arrange
        Document doc1 = new Document("keyField1", "A").append("field", "value1");
        Document doc2 = new Document("keyField1", "A").append("field", "value2");
        List<Document> collection1Docs = Collections.singletonList(doc1);
        List<Document> collection2Docs = Collections.singletonList(doc2);

        // Act
        Map<String, Object> result = documentComparisonService.compareDocuments(collection1Docs, collection2Docs);

        // Assert
        List<Map<String, Object>> differingDocs = (List<Map<String, Object>>) result.get("differingDocs");
        assertEquals(1, differingDocs.size());
        assertEquals("value1", ((Map<String, Object>) differingDocs.getFirst().get("field")).get("collection1"));
        assertEquals("value2", ((Map<String, Object>) differingDocs.getFirst().get("field")).get("collection2"));
    }

    /**
     * Tests that the DocumentComparisonService#compareDocumentFields(Document, Document, Set)}
     * method ignores specified fields during the document comparison process.
     */
    @Test
    void testCompareDocumentFields_IgnoresSpecifiedFields() {
        // Arrange
        ReflectionTestUtils.setField(documentComparisonService, "keyFields", Collections.emptyList());
        Set<String> ignoredFields = new HashSet<>(Collections.singletonList("ignoredField"));
        Document doc1 = new Document("keyField1", "A").append("ignoredField", "ignoredValue1");
        Document doc2 = new Document("keyField1", "B").append("ignoredField", "ignoredValue2");

        // Act
        Map<String, Object> result = ReflectionTestUtils.invokeMethod(
                documentComparisonService, "compareDocumentFields", doc1, doc2, ignoredFields);
        Map<String, Object> expected = new HashMap<>();

        Map<String, Object> keyFieldDiff = new HashMap<>();
        keyFieldDiff.put("collection1", "A");
        keyFieldDiff.put("collection2", "B");
        expected.put("keyField1", keyFieldDiff);


        // Assert
        assertEquals(expected, result);
    }

    /**
     * Tests that the DocumentComparisonService#compareDocumentFields(Document, Document, Set)
     * method correctly compares nested documents, identifying differences at nested levels.
     */
    @Test
    void testCompareDocumentFields_WithNestedDocuments() {
        // Arrange
        ReflectionTestUtils.setField(documentComparisonService, "keyFields", Collections.emptyList());
        Set<String> ignoredFields = Collections.emptySet();
        Document nestedDoc1 = new Document("nestedField", "nestedValue1");
        Document nestedDoc2 = new Document("nestedField", "nestedValue2");

        Document doc1 = new Document("keyField1", "A").append("nestedDoc", nestedDoc1);
        Document doc2 = new Document("keyField1", "A").append("nestedDoc", nestedDoc2);

        // Act
        Map<String, Object> result = ReflectionTestUtils.invokeMethod(
                documentComparisonService, "compareDocumentFields", doc1, doc2, ignoredFields);

        // Assert
        assert result != null;
        assertEquals(1, result.size());

        // Retrieve the nestedDoc differences map
        Map<String, Object> nestedDocDiff = (Map<String, Object>) result.get("nestedDoc");
        Map<String, Object> nestedFieldDiff = (Map<String, Object>) nestedDocDiff.get("nestedField");

        assertEquals("nestedValue1", nestedFieldDiff.get("collection1"));
        assertEquals("nestedValue2", nestedFieldDiff.get("collection2"));
    }
}
