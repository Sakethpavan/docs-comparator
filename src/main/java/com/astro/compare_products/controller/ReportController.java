package com.astro.compare_products.controller;

import com.astro.compare_products.service.DocumentComparisonService;
import com.astro.compare_products.service.DocumentFetcherService;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
public class ReportController {

    @Autowired
    private DocumentFetcherService documentFetcherService;

    @Autowired
    private DocumentComparisonService comparisonService;

    @GetMapping("/generateReport")
    public String generateReport(@RequestParam String upc, @RequestParam String category, Model model) {

        String collection1 = "products"; // Set actual collection names
        String collection2 = "products_salsify";

        // Fetch documents from MongoDB collections
        List<Document> collection1Docs = documentFetcherService.fetchDocuments(category, collection1);
        List<Document> collection2Docs = documentFetcherService.fetchDocuments(category, collection2);

        // compare documents and get comparison report data
        Map<String, Object> reportData = comparisonService.compareDocuments(collection1Docs, collection2Docs);

        model.addAttribute("docsInFirstOnly", reportData.get("docsInFirstOnly"));
        model.addAttribute("docsInSecondOnly", reportData.get("docsInSecondOnly"));
        model.addAttribute("differingDocs", reportData.get("differingDocs"));
        model.addAttribute("collection1", collection1);
        model.addAttribute("collection2", collection2);

        return "report"; // Thymeleaf template name
    }
}
