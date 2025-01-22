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
            String html = "<!DOCTYPE html><html><body><h1>Hello World</h1></body></html>";
            ConversionRequest request = ConversionRequest.builder()
                    .properties(ConversionProperties.builder()
                            .format(PageFormat.A4)
                            .margin(Margin.builder()
                                    .top(20)
                                    .bottom(20)
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