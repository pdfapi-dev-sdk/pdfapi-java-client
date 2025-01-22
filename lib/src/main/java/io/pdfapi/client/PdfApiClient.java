package io.pdfapi.client;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CompletableFuture;

public interface PdfApiClient extends AutoCloseable {
    /**
     * Convert HTML to PDF using the provided conversion request.
     * @param request conversion request containing all necessary data
     * @return PDF content as input stream
     */
    CompletableFuture<InputStream> convert(ConversionRequest request);

    /**
     * Convert HTML to PDF and write the result to the provided output stream.
     * @param request conversion request containing all necessary data
     * @param output stream to write the PDF content to
     */
    default CompletableFuture<Void> convert(ConversionRequest request, OutputStream output) {
        return convert(request).thenAccept(pdfStream -> {
            try (InputStream is = pdfStream) {
                is.transferTo(output);
            } catch (Exception e) {
                throw new PdfApiClientException("Failed to write PDF content", e);
            }
        });
    }

    @Override
    void close();
} 