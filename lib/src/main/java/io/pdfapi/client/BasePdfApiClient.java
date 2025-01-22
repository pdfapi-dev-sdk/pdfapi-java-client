package io.pdfapi.client;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.pdfapi.client.http.HttpClient;
import io.pdfapi.client.model.ConversionProperties;

public class BasePdfApiClient implements PdfApiClient {
    private static final String HEADER_API_KEY = "Api-Key";
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final String apiKey;

    protected BasePdfApiClient(PdfApiClientConfig config, HttpClient httpClient) {
        this.baseUrl = config.getBaseUrl();
        this.apiKey = config.getApiKey();
        this.httpClient = httpClient;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public CompletableFuture<InputStream> convert(ConversionRequest request) {
        return initializeConversion(request.getProperties())
                .thenCompose(conversionId -> {
                    CompletableFuture<Void> assetsFuture = CompletableFuture.completedFuture(null);
                    for (AssetInput asset : request.getAssets()) {
                        assetsFuture = assetsFuture.thenCompose(v -> 
                            attachAsset(conversionId, asset.getContent(), asset.getFileName()));
                    }
                    return assetsFuture
                            .thenCompose(v -> performConversion(conversionId, request.getHtmlContent()))
                            .thenCompose(v -> waitForResult(conversionId));
                });
    }

    private CompletableFuture<String> initializeConversion(ConversionProperties properties) {
        try {
            String json = objectMapper.writeValueAsString(properties);
            return httpClient.post(baseUrl + "/api/conversions", getHeaders(), json)
                    .thenApply(response -> {
                        try {
                            if (response.getStatusCode() != 200) {
                                throw new PdfApiClientException("Failed to initialize conversion: " + response.getBodyAsString());
                            }
                            return objectMapper.readTree(response.getBodyAsString())
                                    .get("id").asText();
                        } catch (JsonProcessingException | PdfApiClientException e) {
                            throw new PdfApiClientException("Failed to parse conversion ID", e);
                        }
                    });
        } catch (JsonProcessingException e) {
            return CompletableFuture.failedFuture(
                    new PdfApiClientException("Failed to serialize conversion properties", e));
        }
    }

    private CompletableFuture<Void> attachAsset(String conversionId, InputStream assetStream, String fileName) {
        return httpClient.post(
                baseUrl + "/api/conversions/" + conversionId + "/assets",
                getHeaders(),
                fileName,
                assetStream,
                "application/octet-stream"
        ).thenApply(response -> {
            if (response.getStatusCode() != 200) {
                throw new PdfApiClientException("Failed to attach asset: " + response.getBodyAsString());
            }
            return null;
        });
    }

    private CompletableFuture<Void> performConversion(String conversionId, InputStream htmlContent) {
        return httpClient.post(
                baseUrl + "/api/conversions/" + conversionId + "/convert",
                getHeaders(),
                "index.html",
                htmlContent,
                "text/html"
        ).thenApply(response -> {
            if (response.getStatusCode() != 200) {
                throw new PdfApiClientException("Failed to perform conversion: " + response.getBodyAsString());
            }
            return null;
        });
    }

    private CompletableFuture<InputStream> waitForResult(String conversionId) {
        return getConversionResult(conversionId)
                .thenCompose(result -> {
                    if (result == null) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return CompletableFuture.failedFuture(e);
                        }
                        return waitForResult(conversionId);
                    }
                    return CompletableFuture.completedFuture(result);
                });
    }

    private CompletableFuture<InputStream> getConversionResult(String conversionId) {
        return httpClient.get(baseUrl + "/api/conversions/" + conversionId, getHeaders())
                .thenApply(response -> {
                    if (response.getStatusCode() == 204) {
                        return null;
                    }
                    if (response.getStatusCode() != 200) {
                        throw new PdfApiClientException("Failed to get conversion result: " + response.getBodyAsString());
                    }
                    return response.getBodyAsStream();
                });
    }

    private Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put(HEADER_API_KEY, apiKey);
        return Collections.unmodifiableMap(headers);
    }

    @Override
    public void close() {
        httpClient.close();
    }
} 