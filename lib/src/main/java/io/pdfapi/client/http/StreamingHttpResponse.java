package io.pdfapi.client.http;

import java.io.InputStream;

public class StreamingHttpResponse implements HttpResponse {
    private final int statusCode;
    private final InputStream bodyStream;
    private final AutoCloseable responseToClose;

    public StreamingHttpResponse(int statusCode, InputStream bodyStream, AutoCloseable responseToClose) {
        this.statusCode = statusCode;
        this.bodyStream = bodyStream;
        this.responseToClose = responseToClose;
    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public InputStream getBodyAsStream() {
        return bodyStream;
    }

    @Override
    public void close() {
        try {
            if (bodyStream != null) {
                bodyStream.close();
            }
            if (responseToClose != null) {
                responseToClose.close();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to close response", e);
        }
    }
} 
