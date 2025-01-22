package io.pdfapi.client.http;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class SimpleHttpResponse implements HttpResponse {
    private final int statusCode;
    private final String body;

    public SimpleHttpResponse(int statusCode, String body) {
        this.statusCode = statusCode;
        this.body = body;
    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public String getBodyAsString() {
        return body;
    }

    @Override
    public InputStream getBodyAsStream() {
        return body != null ? new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)) : null;
    }

    @Override
    public void close() {
        // Nothing to close in this implementation
    }
} 