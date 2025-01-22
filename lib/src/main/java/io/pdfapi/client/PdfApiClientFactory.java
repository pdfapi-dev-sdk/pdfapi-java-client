package io.pdfapi.client;

import org.apache.http.impl.client.CloseableHttpClient;

import io.pdfapi.client.http.ApacheHttpClient;
import io.pdfapi.client.http.HttpClient;
import io.pdfapi.client.http.OkHttpClient;

public class PdfApiClientFactory {
    /**
     * Creates a new instance of PdfApiClient using the default HTTP client implementation (OkHttp).
     * For specific HTTP client implementations, use {@link #createWithOkHttp(PdfApiClientConfig)} or
     * {@link #createWithApacheHttpClient(PdfApiClientConfig)}.
     * 
     * @param config client configuration
     * @return new client instance
     */
    public static PdfApiClient createClient(PdfApiClientConfig config) {
        return createWithOkHttp(config);
    }

    /**
     * Creates a new instance of PdfApiClient using OkHttp implementation with default configuration.
     * This implementation is optimized for performance and is the recommended choice
     * for most use cases.
     * 
     * @param config client configuration
     * @return new client instance
     */
    public static PdfApiClient createWithOkHttp(PdfApiClientConfig config) {
        return new BasePdfApiClient(config, new OkHttpClient(config.getTimeoutSeconds()));
    }

    /**
     * Creates a new instance of PdfApiClient using a pre-configured OkHttp client.
     * Use this when you need to customize the OkHttp client configuration beyond timeouts.
     * 
     * @param config client configuration
     * @param okHttpClient pre-configured OkHttp client instance
     * @return new client instance
     */
    public static PdfApiClient createWithOkHttp(PdfApiClientConfig config, okhttp3.OkHttpClient okHttpClient) {
        return new BasePdfApiClient(config, new OkHttpClient(okHttpClient));
    }

    /**
     * Creates a new instance of PdfApiClient using Apache HTTP Client implementation with default configuration.
     * This implementation is provided as an alternative for environments where
     * Apache HTTP Client is preferred or already in use.
     * 
     * @param config client configuration
     * @return new client instance
     */
    public static PdfApiClient createWithApacheHttpClient(PdfApiClientConfig config) {
        return new BasePdfApiClient(config, new ApacheHttpClient());
    }

    /**
     * Creates a new instance of PdfApiClient using a pre-configured Apache HTTP client.
     * Use this when you need to customize the Apache HTTP client configuration.
     * 
     * @param config client configuration
     * @param httpClient pre-configured Apache HTTP client instance
     * @return new client instance
     */
    public static PdfApiClient createWithApacheHttpClient(PdfApiClientConfig config, CloseableHttpClient httpClient) {
        return new BasePdfApiClient(config, new ApacheHttpClient(httpClient));
    }

    /**
     * Creates a new instance of PdfApiClient using custom HTTP client implementation.
     * Use this method when you need to provide your own HTTP client implementation
     * that conforms to the {@link HttpClient} interface.
     * 
     * @param config client configuration
     * @param httpClient custom HTTP client implementation
     * @return new client instance
     */
    public static PdfApiClient createWithCustomHttpClient(PdfApiClientConfig config, HttpClient httpClient) {
        return new BasePdfApiClient(config, httpClient);
    }
} 