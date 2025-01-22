package io.pdfapi.client.http;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractHttpClient implements HttpClient {
    protected abstract CompletableFuture<HttpResponse> executePost(String url, Map<String, String> headers, String jsonBody);
    protected abstract CompletableFuture<HttpResponse> executePost(String url, Map<String, String> headers, String fileName, InputStream content, String contentType);
    protected abstract CompletableFuture<HttpResponse> executeGet(String url, Map<String, String> headers);
    protected abstract void closeInternal();

    @Override
    public CompletableFuture<HttpResponse> post(String url, Map<String, String> headers, String jsonBody) {
        return executePost(url, headers, jsonBody)
            .exceptionally(e -> {
                throw new RuntimeException("Failed to execute POST request", e);
            });
    }

    @Override
    public CompletableFuture<HttpResponse> post(String url, Map<String, String> headers, String fileName, InputStream content, String contentType) {
        return executePost(url, headers, fileName, content, contentType)
            .exceptionally(e -> {
                throw new RuntimeException("Failed to execute POST request with file", e);
            });
    }

    @Override
    public CompletableFuture<HttpResponse> get(String url, Map<String, String> headers) {
        return executeGet(url, headers)
            .exceptionally(e -> {
                throw new RuntimeException("Failed to execute GET request", e);
            });
    }

    @Override
    public void close() {
        try {
            closeInternal();
        } catch (Exception e) {
            throw new RuntimeException("Failed to close HTTP client", e);
        }
    }
} 