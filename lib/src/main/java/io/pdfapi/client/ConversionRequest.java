package io.pdfapi.client;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.pdfapi.client.model.ConversionProperties;
import io.pdfapi.client.model.PageFormat;

public class ConversionRequest {
    private final ConversionProperties properties;
    private final InputStream htmlContent;
    private final List<AssetInput> assets;
    private final AssetInput headerFile;
    private final AssetInput footerFile;

    private ConversionRequest(Builder builder) {
        this.properties = Objects.requireNonNull(builder.properties, "Properties must not be null");
        this.htmlContent = Objects.requireNonNull(builder.htmlContent, "HTML content must not be null");
        this.assets = List.copyOf(builder.assets);
        this.headerFile = builder.headerFile;
        this.footerFile = builder.footerFile;
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

    public AssetInput getHeaderFile() {
        return headerFile;
    }

    public AssetInput getFooterFile() {
        return footerFile;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ConversionProperties properties;
        private InputStream htmlContent;
        private final List<AssetInput> assets = new ArrayList<>();
        private AssetInput headerFile;
        private AssetInput footerFile;

        public Builder properties(ConversionProperties properties) {
            this.properties = Objects.requireNonNull(properties, "Properties must not be null");
            return this;
        }

        public Builder htmlContent(InputStream htmlContent) {
            this.htmlContent = Objects.requireNonNull(htmlContent, "HTML content must not be null");
            return this;
        }

        public Builder addAsset(AssetInput asset) {
            this.assets.add(Objects.requireNonNull(asset, "Asset must not be null"));
            return this;
        }

        public Builder headerFile(AssetInput headerFile) {
            this.headerFile = Objects.requireNonNull(headerFile, "Header file must not be null");
            this.assets.add(headerFile);
            if (this.properties != null) {
                this.properties = ConversionProperties.builderFrom(properties)
                    .headerFile(headerFile.getFileName())
                    .build();
            }
            return this;
        }

        public Builder footerFile(AssetInput footerFile) {
            this.footerFile = Objects.requireNonNull(footerFile, "Footer file must not be null");
            this.assets.add(footerFile);
            if (this.properties != null) {
                this.properties = ConversionProperties.builderFrom(properties)
                    .footerFile(footerFile.getFileName())
                    .build();
            }
            return this;
        }

        public ConversionRequest build() {
            if (properties == null) {
                properties = ConversionProperties.builder()
                    .format(PageFormat.A4)  // Set a default format since it's required
                    .build();
            }
            
            // Update properties with header/footer filenames if they exist
            if ((headerFile != null || footerFile != null) && properties != null) {
                ConversionProperties.Builder propsBuilder = ConversionProperties.builderFrom(properties);

                if (headerFile != null) {
                    propsBuilder.headerFile(headerFile.getFileName());
                }
                if (footerFile != null) {
                    propsBuilder.footerFile(footerFile.getFileName());
                }

                properties = propsBuilder.build();
            }

            return new ConversionRequest(this);
        }
    }

    public static class AssetInput {
        private final InputStream content;
        private final String fileName;

        public AssetInput(InputStream content, String fileName) {
            this.content = content;
            this.fileName = fileName;
        }

        public InputStream getContent() {
            return content;
        }

        public String getFileName() {
            return fileName;
        }

        public static AssetInput of(InputStream content, String fileName) {
            return new AssetInput(content, fileName);
        }
    }
}
