package io.pdfapi.client.model;

public class ConversionProperties {
    private final PageFormat format;
    private final String headerFile;
    private final String footerFile;
    private final float scale;
    private final Margin margin;
    private final boolean landscape;

    private ConversionProperties(Builder builder) {
        this.format = builder.format;
        this.headerFile = builder.headerFile;
        this.footerFile = builder.footerFile;
        this.scale = builder.scale;
        this.margin = builder.margin;
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

    public static class Builder {
        private PageFormat format;
        private String headerFile;
        private String footerFile;
        private float scale = 1.0f;
        private Margin margin;
        private boolean landscape;

        public Builder format(PageFormat format) {
            this.format = format;
            return this;
        }

        public Builder headerFile(String headerFile) {
            this.headerFile = headerFile;
            return this;
        }

        public Builder footerFile(String footerFile) {
            this.footerFile = footerFile;
            return this;
        }

        public Builder scale(float scale) {
            this.scale = scale;
            return this;
        }

        public Builder margin(Margin margin) {
            this.margin = margin;
            return this;
        }

        public Builder landscape(boolean landscape) {
            this.landscape = landscape;
            return this;
        }

        public ConversionProperties build() {
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