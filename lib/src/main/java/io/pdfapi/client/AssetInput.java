package io.pdfapi.client;

import java.io.InputStream;

public class AssetInput {
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