package io.pdfapi.client.http;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class StreamingHttpResponse implements HttpResponse {
    private final int statusCode;
    private final InputStream bodyStream;
    private final AutoCloseable responseToClose;
    private final Map<String, List<String>> headers;

    public StreamingHttpResponse(int statusCode, InputStream bodyStream, AutoCloseable responseToClose, Map<String, List<String>> headers) {
        this.statusCode = statusCode;
        this.bodyStream = bodyStream;
        this.responseToClose = responseToClose;
        this.headers = headers;
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
    public Optional<String> getLocationHeader() {
        return headers.get("Location").stream().findFirst();
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
