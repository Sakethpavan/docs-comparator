package com.astro.compare_products.controller;

import com.astro.compare_products.service.DocumentComparisonService;
import com.astro.compare_products.service.DocumentFetcherService;
import org.bson.Document;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.astro.compare_products.common.FileUtils.writeToFile;

@Controller
public class ReportController {

    private final DocumentFetcherService documentFetcherService;
    private final DocumentComparisonService comparisonService;
    private final SpringTemplateEngine templateEngine;

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

        // Set up the Thymeleaf context for the report
        Context context = new Context();
        context.setVariable("docsInFirstOnly", reportData.get("docsInFirstOnly"));
        context.setVariable("docsInSecondOnly", reportData.get("docsInSecondOnly"));
        context.setVariable("differingDocs", reportData.get("differingDocs"));
        context.setVariable("collection1", collection1);
        context.setVariable("collection2", collection2);
        String htmlContent = templateEngine.process("report", context);

        // Save the HTML content to a file
        writeToFile("comparison_report.html", htmlContent);

        return "report"; // Thymeleaf template name
    }
}
