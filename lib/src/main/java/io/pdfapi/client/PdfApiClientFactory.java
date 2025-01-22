package io.pdfapi.client;

import io.pdfapi.client.http.ApacheHttpClient;
import io.pdfapi.client.http.HttpClient;
import io.pdfapi.client.http.OkHttpClient;

public class PdfApiClientFactory {
    /**
     * Creates a new instance of PdfApiClient using OkHttp implementation.
     * @param config client configuration
     * @return new client instance
     */
    public static PdfApiClient createClient(PdfApiClientConfig config) {
        return createWithOkHttp(config);
    }

    /**
     * Creates a new instance of PdfApiClient using OkHttp implementation.
     * @param config client configuration
     * @return new client instance
     */
    public static PdfApiClient createWithOkHttp(PdfApiClientConfig config) {
        return new BasePdfApiClient(config, new OkHttpClient(config.getTimeoutSeconds()));
    }

    /**
     * Creates a new instance of PdfApiClient using Apache HTTP Client implementation.
     * @param config client configuration
     * @return new client instance
     */
    public static PdfApiClient createWithApacheHttpClient(PdfApiClientConfig config) {
        return new BasePdfApiClient(config, new ApacheHttpClient());
    }

    /**
     * Creates a new instance of PdfApiClient using custom HTTP client implementation.
     * @param config client configuration
     * @param httpClient custom HTTP client implementation
     * @return new client instance
     */
    public static PdfApiClient createWithCustomHttpClient(PdfApiClientConfig config, HttpClient httpClient) {
        return new BasePdfApiClient(config, httpClient);
    }
} 