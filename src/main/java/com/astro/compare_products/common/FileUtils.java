package com.astro.compare_products.common;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtils {

    /**
     * Utility method to write HTML content to a file.
     *
     * @param fileName    The name of the file to be created.
     * @param htmlContent The HTML content to be written.
     * @throws IOException If there is an error writing the file.
     */
    public static void writeToFile(String fileName, String htmlContent) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(htmlContent);
        }
    }
}