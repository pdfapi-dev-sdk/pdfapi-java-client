package io.pdfapi.client;

public class PdfApiClientConfig {
    private final String baseUrl;
    private final String apiKey;
    private final int timeoutSeconds;

    private PdfApiClientConfig(Builder builder) {
        this.baseUrl = builder.baseUrl;
        this.apiKey = builder.apiKey;
        this.timeoutSeconds = builder.timeoutSeconds;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String baseUrl = "https://api.pdfapi.io";
        private String apiKey;
        private int timeoutSeconds = 30;

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder timeoutSeconds(int timeoutSeconds) {
            this.timeoutSeconds = timeoutSeconds;
            return this;
        }

        public PdfApiClientConfig build() {
            if (apiKey == null || apiKey.isEmpty()) {
                throw new IllegalStateException("API key must be provided");
            }
            return new PdfApiClientConfig(this);
        }
    }
} 