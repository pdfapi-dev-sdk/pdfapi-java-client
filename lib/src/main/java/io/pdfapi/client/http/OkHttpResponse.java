package io.pdfapi.client.http;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.Response;
import okhttp3.ResponseBody;

public class OkHttpResponse implements HttpResponse {
    private final Response response;
    private final ResponseBody body;

    public OkHttpResponse(Response response) {
        this.response = response;
        this.body = response.body();
    }

    @Override
    public int getStatusCode() {
        return response.code();
    }

    @Override
    public String getBodyAsString() {
        try {
            return body != null ? body.string() : null;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read response body", e);
        }
    }

    @Override
    public InputStream getBodyAsStream() {
        return body != null ? body.byteStream() : null;
    }

    @Override
    public void close() {
        response.close();
    }
} 