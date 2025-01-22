package io.pdfapi.client.http;

import java.io.InputStream;

public interface HttpResponse extends AutoCloseable {
    int getStatusCode();
    String getBodyAsString();
    InputStream getBodyAsStream();
} 