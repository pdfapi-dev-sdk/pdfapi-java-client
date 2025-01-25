package io.pdfapi.client.model;

import java.util.Objects;

public class ConversionProperties {
    private final PageFormat format;
    private final String headerFile;
    private final String footerFile;
    private final float scale;
    private final Margin margin;
    private final boolean landscape;

    private ConversionProperties(Builder builder) {
        this.format = Objects.requireNonNull(builder.format, "Format must not be null");
        this.headerFile = builder.headerFileName;
        this.footerFile = builder.footerFileName;
        this.scale = builder.scale;
        this.margin = Objects.requireNonNull(builder.margin, "Margin must not be null");
        this.landscape = builder.landscape;
    }

    public PageFormat getFormat() {
        return format;
    }

    public String getHeaderFile() {
        return headerFile;
    }

    public String getFooterFile() {
        return footerFile;
    }

    public float getScale() {
        return scale;
    }

    public Margin getMargin() {
        return margin;
    }

    public boolean isLandscape() {
        return landscape;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builderFrom(ConversionProperties properties) {
        Objects.requireNonNull(properties, "Properties must not be null");
        return builder()
            .format(properties.format)
            .scale(properties.scale)
            .margin(properties.margin)
            .landscape(properties.landscape)
            .headerFile(properties.headerFile)
            .footerFile(properties.footerFile);
    }

    public static class Builder {
        private PageFormat format;
        private String headerFileName;
        private String footerFileName;
        private float scale = 1.0f;
        private Margin margin;
        private boolean landscape;

        public Builder format(PageFormat format) {
            this.format = Objects.requireNonNull(format, "Format must not be null");
            return this;
        }

        public Builder headerFile(String headerFileName) {
            this.headerFileName = headerFileName;
            return this;
        }

        public Builder footerFile(String footerFileName) {
            this.footerFileName = footerFileName;
            return this;
        }

        public Builder scale(float scale) {
            this.scale = scale;
            return this;
        }

        public Builder margin(Margin margin) {
            this.margin = Objects.requireNonNull(margin, "Margin must not be null");
            return this;
        }

        public Builder landscape(boolean landscape) {
            this.landscape = landscape;
            return this;
        }

        public ConversionProperties build() {
            if (format == null) {
                throw new IllegalStateException("Format must be set");
            }
            if (margin == null) {
                margin = Margin.builder()
                    .top(0)
                    .bottom(0)
                    .left(0)
                    .right(0)
                    .build();
            }
            return new ConversionProperties(this);
        }
    }
} 