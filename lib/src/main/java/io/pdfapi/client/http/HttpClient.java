package io.pdfapi.client.http;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface HttpClient {
    CompletableFuture<HttpResponse> post(String url, Map<String, String> headers, String jsonBody);
    CompletableFuture<HttpResponse> post(String url, Map<String, String> headers, String fileName, InputStream content, String contentType);
    CompletableFuture<HttpResponse> get(String url, Map<String, String> headers);
    void close();
} 