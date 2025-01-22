package io.pdfapi.client;

public class PdfApiClientException extends RuntimeException {
    public PdfApiClientException(String message) {
        super(message);
    }

    public PdfApiClientException(String message, Throwable cause) {
        super(message, cause);
    }
} 