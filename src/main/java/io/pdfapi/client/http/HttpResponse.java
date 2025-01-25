package io.pdfapi.client.http;

import java.io.InputStream;
import java.util.Optional;

public interface HttpResponse extends AutoCloseable {
    int getStatusCode();

    InputStream getBodyAsStream();

    Optional<String> getLocationHeader();
}
