package io.pdfapi.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.pdfapi.client.http.HttpClient;
import io.pdfapi.client.http.HttpResponse;
import io.pdfapi.client.model.ConversionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class PdfApiClient implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(PdfApiClient.class);
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

    protected PdfApiClient(PdfApiClientConfig config, HttpClient httpClient) {
        this.baseUrl = config.getBaseUrl();
        this.apiKey = config.getApiKey();
        this.httpClient = httpClient;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Convert HTML to PDF using the provided conversion request.
     *
     * @param request conversion request containing all necessary data
     * @return PDF content as input stream
     */
    public CompletableFuture<InputStream> convert(ConversionRequest request) {
        logger.info("Starting PDF conversion");
        return initializeConversion(request.getProperties())
                .thenCompose(conversionId -> {
                    logger.debug("Conversion initialized with ID: {}", conversionId);
                    return uploadAssetsInParallel(conversionId, request.getAssets())
                            .thenCompose(v -> {
                                logger.debug("Assets uploaded for conversion {}", conversionId);
                                return performConversion(conversionId, request.getHtmlContent());
                            })
                            .thenCompose(v -> {
                                logger.debug("Starting to wait for conversion result {}", conversionId);
                                return waitForResult(conversionId);
                            });
                });
    }

    /**
     * Convert HTML to PDF and write the result to the provided output stream.
     *
     * @param request conversion request containing all necessary data
     * @param output  stream to write the PDF content to
     */
    CompletableFuture<Void> convert(ConversionRequest request, OutputStream output) {
        return convert(request).thenAccept(pdfStream -> {
            try (InputStream is = pdfStream) {
                is.transferTo(output);
            } catch (Exception e) {
                throw new PdfApiClientException("Failed to write PDF content", e);
            }
        });
    }

    /**
     * Convert HTML to PDF and write the result to the provided output stream sync.
     *
     * @param request conversion request containing all necessary data
     * @param output  stream to write the PDF content to
     */
    void convertSync(ConversionRequest request, OutputStream output) {
        convert(request, output).join();
    }

    private CompletableFuture<String> initializeConversion(ConversionProperties properties) {
        try {
            String json = objectMapper.writeValueAsString(properties);
            logger.debug("Initializing conversion with properties: {}", json);
            return httpClient.post(baseUrl + PATH_CONVERSIONS, getHeaders(), json)
                    .thenApply(response -> parseJsonResponse(response, "id"));
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize conversion properties", e);
            return CompletableFuture.failedFuture(
                    new PdfApiClientException("Failed to serialize conversion properties", e));
        }
    }

    private CompletableFuture<Void> uploadAssetsInParallel(String conversionId, List<ConversionRequest.AssetInput> assets) {
        logger.debug("Uploading {} assets for conversion {}", assets.size(), conversionId);
        List<CompletableFuture<Void>> uploads = assets.stream()
                .map(asset -> attachAsset(conversionId, asset.getContent(), asset.getFileName()))
                .collect(Collectors.toList());

        return CompletableFuture.allOf(uploads.toArray(CompletableFuture[]::new));
    }

    private CompletableFuture<Void> attachAsset(String conversionId, InputStream assetStream, String fileName) {
        logger.debug("Attaching asset {} to conversion {}", fileName, conversionId);
        return httpClient.post(
                baseUrl + PATH_CONVERSIONS + "/" + conversionId + PATH_ASSETS,
                getHeaders(),
                fileName,
                assetStream,
                "application/octet-stream",
                "asset"
        ).thenApply(response -> {
            handleResponse(response);
            return null;
        });
    }

    private CompletableFuture<Void> performConversion(String conversionId, InputStream htmlContent) {
        logger.debug("Starting conversion for ID: {}", conversionId);
        return httpClient.post(
                baseUrl + PATH_CONVERSIONS + "/" + conversionId + PATH_CONVERT,
                getHeaders(),
                "index.html",
                htmlContent,
                "text/html",
                "index"
        ).thenApply(response -> {
            handleResponse(response);
            logger.debug("Conversion started successfully for ID: {}", conversionId);
            return null;
        });
    }

    private CompletableFuture<InputStream> waitForResult(String conversionId) {
        return waitForResultWithBackoff(conversionId, INITIAL_POLLING_DELAY_MS);
    }

    private CompletableFuture<InputStream> waitForResultWithBackoff(String conversionId, long currentDelay) {
        logger.trace("Checking conversion status for {} with delay {}ms", conversionId, currentDelay);
        return getConversionResult(conversionId)
                .thenCompose(result -> {
                    if (result == null) {
                        logger.trace("Conversion {} still in progress, next check in {}ms", conversionId,
                                Math.min((long) (currentDelay * BACKOFF_MULTIPLIER), MAX_POLLING_DELAY_MS));
                        CompletableFuture<Void> delay = new CompletableFuture<>();
                        CompletableFuture.delayedExecutor(currentDelay, TimeUnit.MILLISECONDS)
                                .execute(() -> delay.complete(null));
                        return delay.thenCompose(v -> waitForResultWithBackoff(conversionId,
                                Math.min((long) (currentDelay * BACKOFF_MULTIPLIER), MAX_POLLING_DELAY_MS)));
                    }
                    logger.info("Conversion {} completed successfully", conversionId);
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
            return objectMapper.readTree(response.getBodyAsStream()).get(field).asText();
        } catch (IOException e) {
            throw new PdfApiClientException("Failed to parse JSON response", e);
        }
    }

    private HttpResponse handleResponse(HttpResponse response) {
        if (response.getStatusCode() != 200 && response.getStatusCode() != 201 && response.getStatusCode() != 204) {
            logger.error("Request failed with status {}", response.getStatusCode());
            throw new PdfApiClientException("Request failed with status " + response.getStatusCode());
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
        logger.debug("Closing PDF API client");
        httpClient.close();
    }
} 
