package com.astro.compare_products.service;

import jakarta.annotation.PostConstruct;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.astro.compare_products.common.Constants.*;

/**
 * Service for comparing documents from two collections, identifying documents unique to each collection,
 * and determining field-level differences between matching documents.
 * <p>
 * This service reads configuration properties for fields to ignore during comparison and key fields
 * for identifying matching documents across collections.
 */
@Service
public class DocumentComparisonService {

    Logger logger = LoggerFactory.getLogger(DocumentComparisonService.class);

    // Inject ignored fields from the application properties
    @Value("${comparison.ignoredFields}")
    private String ignoredFieldsProperty;

    // Inject key fields from the application properties
    @Value("${comparison.keyFields}")
    private String keyFieldsProperty;

    private List<String> keyFields;

    /**
     * Initializes the service by splitting the configured key fields property
     * into a list for use in document key generation.
     */
    @PostConstruct
    public void init() {
        // Initialize keyFields by splitting the YAML property
        keyFields = Arrays.asList(keyFieldsProperty.split(","));
    }

    /**
     * Compares two lists of documents, identifying documents unique to each collection
     * and any field-level differences in matching documents.
     *
     * @param collection1Docs List of documents in the first collection
     * @param collection2Docs List of documents in the second collection
     * @return Map containing lists of documents unique to each collection and any differing documents
     */
    public Map<String, Object> compareDocuments(List<Document> collection1Docs, List<Document> collection2Docs) {
        // Maps to store comparison results
        List<Document> docsInFirstOnly = new ArrayList<>();
        List<Document> docsInSecondOnly = new ArrayList<>();
        List<Map<String, Object>> differingDocs = new ArrayList<>();

        Map<String, Object> reportData = new HashMap<>();

        // Split the ignored fields into a Set for easy lookup
        Set<String> ignoredFields = new HashSet<>(Arrays.asList(ignoredFieldsProperty.split(",")));

        // Compare documents in both collections
        compareDocumentLists(collection1Docs, collection2Docs, docsInFirstOnly, differingDocs, ignoredFields);
        compareDocumentLists(collection2Docs, collection1Docs, docsInSecondOnly, null, ignoredFields);


        reportData.put("docsInFirstOnly", docsInFirstOnly);
        reportData.put("docsInSecondOnly", docsInSecondOnly);
        reportData.put("differingDocs", differingDocs);

        return reportData;
    }

    /**
     * Compares fields between two documents, ignoring specified fields and performing deep comparison
     * on nested documents.
     *
     * @param doc1          The first document
     * @param doc2          The second document
     * @param ignoredFields Set of fields to ignore during comparison
     * @return Map of fields with differing values between the two documents
     */
    private Map<String, Object> compareDocumentFields(Document doc1, Document doc2, Set<String> ignoredFields) {
        Map<String, Object> differences = new HashMap<>();

        // Add the KEY_FIELDS to the differences map to be displayed later
        keyFields.forEach(keyField -> {
            Map<String, Object> diff = new HashMap<>();
            diff.put("collection1", doc1.get(keyField));
            diff.put("collection2", doc2.get(keyField));
            differences.put(keyField, diff);
        });

        // Use streams to iterate over the keys of doc1
        doc1.keySet().stream()
                .filter(key -> !ignoredFields.contains(key)) // Skip ignored fields
                .forEach(key -> {
                    Object value1 = doc1.get(key);
                    Object value2 = doc2.get(key);

                    if (value1 instanceof Document && value2 instanceof Document) {
                        // If both values are Documents, perform a deep comparison
                        Map<String, Object> nestedDifferences = compareDocumentFields((Document) value1, (Document) value2, ignoredFields);
                        if (!nestedDifferences.isEmpty()) {
                            differences.put(key, nestedDifferences);
                        }
                    } else if (!Objects.equals(value1, value2)) {
                        // For non-document values, check for equality
                        Map<String, Object> diff = new HashMap<>();
                        diff.put("collection1", value1);
                        diff.put("collection2", value2);
                        differences.put(key, diff);
                    }
                });

        return differences;
    }

    /**
     * Helper method to compare documents from one collection against documents in another collection,
     * identifying unique documents and collecting field differences for matching documents.
     *
     * @param sourceDocs       List of documents from the source collection
     * @param targetDocs       List of documents from the target collection
     * @param docsInSourceOnly List to store documents unique to the source collection
     * @param differingDocs    List to store field-level differences for matching documents
     * @param ignoredFields    Set of fields to ignore during comparison
     */
    private void compareDocumentLists(List<Document> sourceDocs, List<Document> targetDocs,
                                      List<Document> docsInSourceOnly, List<Map<String, Object>> differingDocs,
                                      Set<String> ignoredFields) {
        // Create a map for fast lookup of target documents by their key
        Map<String, Document> targetDocMap = targetDocs.stream()
                .collect(Collectors.toMap(
                        this::generateKey,
                        Function.identity(),
                        (existing, replacement) -> {
                            logger.warn("Duplicate key found: {}. Existing document: {}. New Document: {}", generateKey(existing), existing, replacement);
                            return existing;
                        })
                );

        // Process each document from the source collection
        sourceDocs
                .forEach(sourceDoc -> {
                    String sourceKey = generateKey(sourceDoc);
                    Document matchingTargetDoc = targetDocMap.get(sourceKey);

                    if (matchingTargetDoc != null) {
                        // If a match is found, compare the fields and collect differences
                        if (differingDocs != null) {
                            Map<String, Object> fieldDifferences = compareDocumentFields(sourceDoc, matchingTargetDoc, ignoredFields);
                            if (!fieldDifferences.isEmpty()) {
                                differingDocs.add(fieldDifferences);
                            }
                        }
                    } else {
                        // If no match is found, add to docsInSourceOnly
                        docsInSourceOnly.add(sourceDoc);
                    }
                });
    }

    /**
     * Generates a unique key for a document by concatenating values of key fields,
     * allowing documents to be matched across collections.
     *
     * @param doc The document for which to generate the key
     * @return Concatenated string of key field values
     */
    private String generateKey(Document doc) {
        return keyFields.stream()
                .map(field -> doc.get(field) != null ? doc.get(field).toString() : EMPTY_STRING)
                .collect(Collectors.joining(UNDERSCORE));
    }
}
