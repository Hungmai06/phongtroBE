package com.example.room.service;

import org.thymeleaf.context.Context;

public interface PdfGeneratorService {
    void generatePdf(String templateName, Context context, String outputPath);
}
