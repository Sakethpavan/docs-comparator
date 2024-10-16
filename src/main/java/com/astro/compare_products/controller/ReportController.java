package com.astro.compare_products.controller;

import com.astro.compare_products.service.DocumentComparisonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
public class ReportController {

    @Autowired
    private DocumentComparisonService comparisonService;

    @GetMapping("/generateReport")
    public String generateReport(@RequestParam String upc, @RequestParam String category, Model model) {

        String collection1 = "products"; // Set actual collection names
        String collection2 = "products_salsify";

        Map<String, Object> reportData = comparisonService.compareDocuments(upc, category, collection1, collection2);

        model.addAttribute("docsInFirstOnly", reportData.get("docsInFirstOnly"));
        model.addAttribute("docsInSecondOnly", reportData.get("docsInSecondOnly"));
        model.addAttribute("differingDocs", reportData.get("differingDocs"));
        model.addAttribute("collection1", collection1);
        model.addAttribute("collection2", collection2);

        return "report"; // Thymeleaf template name
    }
}
