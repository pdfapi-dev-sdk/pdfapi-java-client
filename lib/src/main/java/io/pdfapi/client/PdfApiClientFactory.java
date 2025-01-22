package io.pdfapi.client;

import io.pdfapi.client.http.OkHttpClient;

public class PdfApiClientFactory {
    /**
     * Creates a new instance of PdfApiClient using OkHttp implementation.
     * @param config client configuration
     * @return new client instance
     */
    public static PdfApiClient createClient(PdfApiClientConfig config) {
        return new BasePdfApiClient(config, new OkHttpClient(config.getTimeoutSeconds()));
    }
} 