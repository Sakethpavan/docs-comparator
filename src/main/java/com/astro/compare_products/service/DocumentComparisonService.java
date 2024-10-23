package com.astro.compare_products.service;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.astro.compare_products.common.Constants.*;

@Service
public class DocumentComparisonService {

    Logger logger = LoggerFactory.getLogger(DocumentComparisonService.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    // Inject ignored fields from the application properties
    @Value("${comparison.ignoredFields}")
    private String ignoredFieldsProperty;

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

    // Helper method to compare fields of two documents
    private Map<String, Object> compareDocumentFields(Document doc1, Document doc2, Set<String> ignoredFields) {
        Map<String, Object> differences = new HashMap<>();

        // Add the UPC to the differences map to be displayed later
        differences.put("upc", doc1.get("upc"));

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

    // Helper method to compare two document lists and update results
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

    // Helper method to generate the key for document comparison
    private String generateKey(Document doc) {
        String upc = doc.get(FIELD_UPC) != null ? doc.get(FIELD_UPC).toString() : "";
        String catalogType = doc.get(FIELD_CATALOG_TYPE) != null ? doc.get(FIELD_CATALOG_TYPE).toString() : "";
        String country = doc.get(FIELD_COUNTRY) != null ? doc.get(FIELD_COUNTRY).toString() : "";
        String productId = doc.get(FIELD_PRODUCT_ID) != null ? doc.get(FIELD_PRODUCT_ID).toString(): "";
        return upc + UNDERSCORE + productId + UNDERSCORE + catalogType + UNDERSCORE + country;
    }
}
