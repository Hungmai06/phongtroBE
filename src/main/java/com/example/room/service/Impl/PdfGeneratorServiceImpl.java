package com.example.room.service.Impl;

import com.example.room.service.PdfGeneratorService;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class PdfGeneratorServiceImpl implements PdfGeneratorService {
    private final TemplateEngine templateEngine;
    @Override
    public void generatePdf(String templateName, Context context, String outputPath) {
        try {
            // 1. Render HTML từ Thymeleaf
            String htmlContent = templateEngine.process(templateName, context);

            File outputFile = new File(outputPath);
            Path folder = outputFile.getParentFile().toPath();
            if (!Files.exists(folder)) {
                Files.createDirectories(folder);
            }

            try (OutputStream os = new FileOutputStream(outputFile)) {
                PdfRendererBuilder builder = new PdfRendererBuilder();

                // 2. baseUri để @font-face url('fonts/...') hoạt động
                String baseUri = getClass().getResource("/").toString();
                builder.withHtmlContent(htmlContent, baseUri);

                // 3. Nhúng font Roboto từ classpath: /resources/fonts/Roboto-VariableFont_wdth,wght.ttf
                builder.useFont(
                        () -> getClass().getResourceAsStream("/fonts/Roboto-VariableFont_wdth,wght.ttf"),
                        "Roboto",
                        400,
                        PdfRendererBuilder.FontStyle.NORMAL,
                        true
                );
                builder.useFont(
                        () -> getClass().getResourceAsStream("/fonts/Roboto-VariableFont_wdth,wght.ttf"),
                        "Roboto",
                        700,
                        PdfRendererBuilder.FontStyle.NORMAL,
                        true
                );

                builder.useFastMode();
                builder.toStream(os);
                builder.run();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF: " + e.getMessage(), e);
        }
    }
}
