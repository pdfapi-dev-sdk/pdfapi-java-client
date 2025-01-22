package io.pdfapi.client;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.pdfapi.client.http.HttpClient;
import io.pdfapi.client.http.HttpResponse;
import io.pdfapi.client.model.ConversionProperties;

public class BasePdfApiClient implements PdfApiClient {
    private static final String HEADER_API_KEY = "Api-Key";
    private static final long INITIAL_POLLING_DELAY_MS = 500;
    private static final long MAX_POLLING_DELAY_MS = 5000;
    private static final float BACKOFF_MULTIPLIER = 1.5f;
    
    private static final String PATH_CONVERSIONS = "/api/conversions";
    private static final String PATH_ASSETS = "/assets";
    private static final String PATH_CONVERT = "/convert";
    
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
                .thenCompose(conversionId -> 
                    uploadAssetsInParallel(conversionId, request.getAssets())
                        .thenCompose(v -> performConversion(conversionId, request.getHtmlContent()))
                        .thenCompose(v -> waitForResult(conversionId))
                );
    }

    private CompletableFuture<String> initializeConversion(ConversionProperties properties) {
        try {
            String json = objectMapper.writeValueAsString(properties);
            return httpClient.post(baseUrl + PATH_CONVERSIONS, getHeaders(), json)
                    .thenApply(response -> parseJsonResponse(response, "id"));
        } catch (JsonProcessingException e) {
            return CompletableFuture.failedFuture(
                    new PdfApiClientException("Failed to serialize conversion properties", e));
        }
    }

    private CompletableFuture<Void> uploadAssetsInParallel(String conversionId, List<AssetInput> assets) {
        List<CompletableFuture<Void>> uploads = assets.stream()
            .map(asset -> attachAsset(conversionId, asset.getContent(), asset.getFileName()))
            .collect(Collectors.toList());
        
        return CompletableFuture.allOf(uploads.toArray(CompletableFuture[]::new));
    }

    private CompletableFuture<Void> attachAsset(String conversionId, InputStream assetStream, String fileName) {
        return httpClient.post(
                baseUrl + PATH_CONVERSIONS + "/" + conversionId + PATH_ASSETS,
                getHeaders(),
                fileName,
                assetStream,
                "application/octet-stream"
        ).thenApply(response -> {
            handleResponse(response);
            return null;
        });
    }

    private CompletableFuture<Void> performConversion(String conversionId, InputStream htmlContent) {
        return httpClient.post(
                baseUrl + PATH_CONVERSIONS + "/" + conversionId + PATH_CONVERT,
                getHeaders(),
                "index.html",
                htmlContent,
                "text/html"
        ).thenApply(response -> {
            handleResponse(response);
            return null;
        });
    }

    private CompletableFuture<InputStream> waitForResult(String conversionId) {
        return waitForResultWithBackoff(conversionId, INITIAL_POLLING_DELAY_MS);
    }

    private CompletableFuture<InputStream> waitForResultWithBackoff(String conversionId, long currentDelay) {
        return getConversionResult(conversionId)
                .thenCompose(result -> {
                    if (result == null) {
                        CompletableFuture<Void> delay = new CompletableFuture<>();
                        CompletableFuture.delayedExecutor(currentDelay, TimeUnit.MILLISECONDS)
                            .execute(() -> delay.complete(null));
                        return delay.thenCompose(v -> waitForResultWithBackoff(conversionId,
                            Math.min((long)(currentDelay * BACKOFF_MULTIPLIER), MAX_POLLING_DELAY_MS)));
                    }
                    return CompletableFuture.completedFuture(result);
                });
    }

    private CompletableFuture<InputStream> getConversionResult(String conversionId) {
        return httpClient.get(baseUrl + PATH_CONVERSIONS + "/" + conversionId, getHeaders())
                .thenApply(response -> {
                    if (response.getStatusCode() == 204) {
                        return null;
                    }
                    return handleResponse(response).getBodyAsStream();
                });
    }

    private String parseJsonResponse(HttpResponse response, String field) {
        handleResponse(response);
        try {
            return objectMapper.readTree(response.getBodyAsString())
                    .get(field).asText();
        } catch (JsonProcessingException e) {
            throw new PdfApiClientException("Failed to parse JSON response", e);
        }
    }

    private HttpResponse handleResponse(HttpResponse response) {
        if (response.getStatusCode() != 200 && response.getStatusCode() != 204) {
            throw new PdfApiClientException("Request failed with status " + response.getStatusCode() + ": " + response.getBodyAsString());
        }
        return response;
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