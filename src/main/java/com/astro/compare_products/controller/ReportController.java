package com.astro.compare_products.controller;

import com.astro.compare_products.service.DocumentComparisonService;
import com.astro.compare_products.service.DocumentFetcherService;
import jakarta.annotation.PostConstruct;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.astro.compare_products.common.Constants.*;
import static com.astro.compare_products.common.FileUtils.writeToFile;

@Controller
public class ReportController {

    private final DocumentFetcherService documentFetcherService;
    private final DocumentComparisonService comparisonService;
    private final SpringTemplateEngine templateEngine;

    // Inject key fields from the application properties
    @Value("${comparison.keyFields}")
    private String keyFieldsProperty;

    private List<String> keyFields;

    @PostConstruct
    public void init() {
        // Initialize keyFields by splitting the YAML property
        keyFields = Arrays.asList(keyFieldsProperty.split(","));
    }

    ReportController(final DocumentFetcherService documentFetcherService,
                     final DocumentComparisonService comparisonService,
                     final SpringTemplateEngine templateEngine
                     ) {
        this.documentFetcherService = documentFetcherService;
        this.comparisonService = comparisonService;
        this.templateEngine = templateEngine;
    }


    @GetMapping("/generateReport")
    public String generateReport(@RequestParam String upc, @RequestParam String category, Model model) throws IOException{

        String collection1 = "products"; // Set actual collection names
        String collection2 = "products_salsify";

        // Build criteria map using the new helper method
        Map<String, String> criteria = buildCriteria(upc, category);

        // Fetch documents from MongoDB collections
        List<Document> collection1Docs = documentFetcherService.fetchDocuments(collection1, criteria);
        List<Document> collection2Docs = documentFetcherService.fetchDocuments(collection2, criteria);

        // compare documents and get comparison report data
        Map<String, Object> reportData = comparisonService.compareDocuments(collection1Docs, collection2Docs);

        model.addAttribute("docsInFirstOnly", reportData.get("docsInFirstOnly"));
        model.addAttribute("docsInSecondOnly", reportData.get("docsInSecondOnly"));
        model.addAttribute("differingDocs", reportData.get("differingDocs"));
        model.addAttribute("collection1", collection1);
        model.addAttribute("collection2", collection2);
        model.addAttribute("keyFields", keyFields);

        // Set up the Thymeleaf context for the report
        Context context = new Context();
        context.setVariable("docsInFirstOnly", reportData.get("docsInFirstOnly"));
        context.setVariable("docsInSecondOnly", reportData.get("docsInSecondOnly"));
        context.setVariable("differingDocs", reportData.get("differingDocs"));
        context.setVariable("collection1", collection1);
        context.setVariable("collection2", collection2);
        context.setVariable("keyFields", keyFields);
        String htmlContent = templateEngine.process("report", context);

        // Save the HTML content to a file
        writeToFile("comparison_report.html", htmlContent);

        return "report"; // Thymeleaf template name
    }

    /**
     * Builds a criteria map based on provided UPC and category.
     *
     * @param upc the UPC code
     * @param category the category
     * @return a map containing the non-empty criteria
     */
    private Map<String, String> buildCriteria(String upc, String category) {
        Map<String, String> criteria = new HashMap<>();

        // Add UPC to criteria only if it's not empty
        if (upc != null && !upc.trim().isEmpty()) {
            criteria.put(INPUT_PARAM_UPC, upc);
        }

        // Add category to criteria only if it's not empty
        if (category != null && !category.trim().isEmpty()) {
            criteria.put(INPUT_PARAM_CATEGORY, category);
        }

        return criteria;
    }
}
