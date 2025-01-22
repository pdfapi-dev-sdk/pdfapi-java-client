package io.pdfapi.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.pdfapi.client.http.HttpClient;
import io.pdfapi.client.http.HttpResponse;
import io.pdfapi.client.http.SimpleHttpResponse;
import io.pdfapi.client.model.ConversionProperties;
import io.pdfapi.client.model.Margin;
import io.pdfapi.client.model.PageFormat;

public class PdfApiClientTest {
    
    private static final String TEST_HTML = "<html><body><h1>Test</h1></body></html>";
    private static final byte[] TEST_PDF = "Mock PDF Content".getBytes(StandardCharsets.UTF_8);
    private PdfApiClientConfig config;
    private ConversionRequest request;
    private TestHttpClient testHttpClient;

    @BeforeEach
    void setUp() {
        config = PdfApiClientConfig.builder()
                .apiKey("test-api-key")
                .baseUrl("http://localhost:8080")
                .build();

        request = ConversionRequest.builder()
                .properties(ConversionProperties.builder()
                        .format(PageFormat.A4)
                        .margin(Margin.builder()
                                .top(20)
                                .bottom(20)
                                .left(20)
                                .right(20)
                                .build())
                        .build())
                .htmlContent(new ByteArrayInputStream(TEST_HTML.getBytes(StandardCharsets.UTF_8)))
                .build();

        testHttpClient = new TestHttpClient();
    }

    @Test
    void testConversionWithDefaultClient() throws Exception {
        try (PdfApiClient client = new BasePdfApiClient(config, testHttpClient)) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            client.convert(request, output).join();
            
            assertNotNull(output.toByteArray());
            // Add more assertions as needed
        }
    }

    /**
     * Test-specific HTTP client implementation that returns predefined responses
     */
    private class TestHttpClient implements HttpClient {
        @Override
        public CompletableFuture<HttpResponse> post(String url, Map<String, String> headers, String jsonBody) {
            // Return conversion ID for initialization request
            return CompletableFuture.completedFuture(
                new SimpleHttpResponse(200, "{\"id\":\"test-conversion-id\"}"));
        }

        @Override
        public CompletableFuture<HttpResponse> post(String url, Map<String, String> headers, String fileName,
                InputStream content, String contentType, String partName) {
            // Return success for asset upload and conversion requests
            return CompletableFuture.completedFuture(new SimpleHttpResponse(200, ""));
        }

        @Override
        public CompletableFuture<HttpResponse> get(String url, Map<String, String> headers) {
            // Return PDF content for result request
            return CompletableFuture.completedFuture(
                new SimpleHttpResponse(200, new String(TEST_PDF, StandardCharsets.UTF_8)));
        }

        @Override
        public void close() {
            // No-op
        }
    }
} 