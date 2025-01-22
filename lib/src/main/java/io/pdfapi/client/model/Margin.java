package io.pdfapi.client.model;

public class Margin {
    private final int top;
    private final int bottom;
    private final int left;
    private final int right;

    private Margin(Builder builder) {
        this.top = builder.top;
        this.bottom = builder.bottom;
        this.left = builder.left;
        this.right = builder.right;
    }

    public int getTop() {
        return top;
    }

    public int getBottom() {
        return bottom;
    }

    public int getLeft() {
        return left;
    }

    public int getRight() {
        return right;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int top;
        private int bottom;
        private int left;
        private int right;

        public Builder top(int top) {
            this.top = top;
            return this;
        }

        public Builder bottom(int bottom) {
            this.bottom = bottom;
            return this;
        }

        public Builder left(int left) {
            this.left = left;
            return this;
        }

        public Builder right(int right) {
            this.right = right;
            return this;
        }

        public Margin build() {
            return new Margin(this);
        }
    }
} 