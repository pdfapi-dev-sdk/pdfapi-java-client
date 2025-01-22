package io.pdfapi.client;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.pdfapi.client.model.ConversionProperties;

public class ConversionRequest {
    private final ConversionProperties properties;
    private final InputStream htmlContent;
    private final List<AssetInput> assets;

    private ConversionRequest(Builder builder) {
        this.properties = builder.properties;
        this.htmlContent = builder.htmlContent;
        this.assets = Collections.unmodifiableList(new ArrayList<>(builder.assets));
    }

    public ConversionProperties getProperties() {
        return properties;
    }

    public InputStream getHtmlContent() {
        return htmlContent;
    }

    public List<AssetInput> getAssets() {
        return assets;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ConversionProperties properties;
        private InputStream htmlContent;
        private final List<AssetInput> assets = new ArrayList<>();

        public Builder properties(ConversionProperties properties) {
            this.properties = properties;
            return this;
        }

        public Builder htmlContent(InputStream htmlContent) {
            this.htmlContent = htmlContent;
            return this;
        }

        public Builder addAsset(InputStream content, String fileName) {
            this.assets.add(AssetInput.of(content, fileName));
            return this;
        }

        public ConversionRequest build() {
            if (properties == null) {
                throw new IllegalStateException("Properties must be set");
            }
            if (htmlContent == null) {
                throw new IllegalStateException("HTML content must be set");
            }
            return new ConversionRequest(this);
        }
    }
} 