package com.astro.compare_products.service;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DocumentComparisonService {

    @Autowired
    private MongoTemplate mongoTemplate;

    public Map<String, Object> compareDocuments(String upc, String category, String collection1Name, String collection2Name) {

        Query query = new Query();
        query.addCriteria(Criteria.where("category").is(category));
//        query.addCriteria(Criteria.where("upc").is(upc).and("category").is(category));

        List<Document> collection1Docs = mongoTemplate.find(query, Document.class, collection1Name);
        List<Document> collection2Docs = mongoTemplate.find(query, Document.class, collection2Name);

        // Maps to store comparison results
        List<Document> docsInFirstOnly = new ArrayList<>();
        List<Document> docsInSecondOnly = new ArrayList<>();
        List<Map<String, Object>> differingDocs = new ArrayList<>();

        Map<String, Object> reportData = new HashMap<>();

        // Compare documents in both collections
        for (Document doc1 : collection1Docs) {
            boolean found = false;
            for (Document doc2 : collection2Docs) {
                if (doc1.get("upc").equals(doc2.get("upc"))) {
                    found = true;
                    // Compare field values of matched documents
                    Map<String, Object> fieldDifferences = compareDocumentFields(doc1, doc2);
                    if (!fieldDifferences.isEmpty()) {
                        differingDocs.add(fieldDifferences);
                    }
                }
            }
            if (!found) {
                docsInFirstOnly.add(doc1);
            }
        }

        // Check for documents in the second collection but not in the first
        for (Document doc2 : collection2Docs) {
            boolean found = false;
            for (Document doc1 : collection1Docs) {
                if (doc2.get("upc").equals(doc1.get("upc"))) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                docsInSecondOnly.add(doc2);
            }
        }

        reportData.put("docsInFirstOnly", docsInFirstOnly);
        reportData.put("docsInSecondOnly", docsInSecondOnly);
        reportData.put("differingDocs", differingDocs);

        return reportData;
    }

    // Helper method to compare fields of two documents
    private Map<String, Object> compareDocumentFields(Document doc1, Document doc2) {
        Map<String, Object> differences = new HashMap<>();

        // Add the UPC to the differences map to be displayed later
        differences.put("upc", doc1.get("upc"));

        Set<String> keys = doc1.keySet();
        for (String key : keys) {
            if (!key.equals("_id") && !key.equals("last_updated")) { // Exclude fields from comparison
                Object value1 = doc1.get(key);
                Object value2 = doc2.get(key);
                if (!Objects.equals(value1, value2)) {
                    Map<String, Object> diff = new HashMap<>();
                    diff.put("collection1", value1);
                    diff.put("collection2", value2);
                    differences.put(key, diff);
                }
            }
        }
        return differences;
    }
}
