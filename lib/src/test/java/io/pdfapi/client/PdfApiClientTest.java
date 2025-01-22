package io.pdfapi.client;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import io.pdfapi.client.model.ConversionProperties;
import io.pdfapi.client.model.Margin;
import io.pdfapi.client.model.PageFormat;

public class PdfApiClientTest {
    
    @Test
    void exampleUsage() throws Exception {
        // Create client configuration
        PdfApiClientConfig config = PdfApiClientConfig.builder()
                .apiKey("your-api-key")
                .build();

        // Create client
        try (PdfApiClient client = PdfApiClientFactory.createClient(config)) {
            // Prepare conversion request
            String html = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>PDF API Test Document</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <h1>Welcome to PDF API</h1>\n" +
                "    <p>This is a test document that demonstrates the PDF API's capabilities. " +
                "It includes custom headers, footers, and styling.</p>\n" +
                "    \n" +
                "    <h1>Features Demonstrated</h1>\n" +
                "    <p>This document shows several features:</p>\n" +
                "    <ul>\n" +
                "        <li>Custom header with page numbers</li>\n" +
                "        <li>Custom footer with date and copyright</li>\n" +
                "        <li>Styled headings and paragraphs</li>\n" +
                "        <li>Responsive layout</li>\n" +
                "        <li>Print-specific styles</li>\n" +
                "    </ul>\n" +
                "    \n" +
                "    <h1>Additional Content</h1>\n" +
                "    <p>This section is added to demonstrate multi-page conversion. " +
                "The header and footer should appear on each page.</p>\n" +
                "    \n" +
                "    <p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do " +
                "eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad " +
                "minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip " +
                "ex ea commodo consequat.</p>\n" +
                "</body>\n" +
                "</html>";

            ConversionRequest request = ConversionRequest.builder()
                    .properties(ConversionProperties.builder()
                            .format(PageFormat.A4)
                            .margin(Margin.builder()
                                    .top(50)    // Space for header
                                    .bottom(50) // Space for footer
                                    .left(20)
                                    .right(20)
                                    .build())
                            .scale(1.0f)
                            .build())
                    .htmlContent(new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8)))
                    .addAsset(AssetInput.of(getClass().getResourceAsStream("/style.css"), "style.css"))
                    .headerFile(AssetInput.of(getClass().getResourceAsStream("/header.html"), "header.html"))
                    .footerFile(AssetInput.of(getClass().getResourceAsStream("/footer.html"), "footer.html"))
                    .build();

            // Convert HTML to PDF and save to file
            try (FileOutputStream output = new FileOutputStream("output.pdf")) {
                client.convert(request, output).join();
            }
        }
    }
} 