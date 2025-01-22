package io.pdfapi.client.http;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class RestTemplateHttpClient extends AbstractHttpClient {
    private final RestTemplate restTemplate;

    public RestTemplateHttpClient() {
        this.restTemplate = new RestTemplate();
    }

    public RestTemplateHttpClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    protected CompletableFuture<HttpResponse> executePost(String url, Map<String, String> headers, String jsonBody) {
        return CompletableFuture.supplyAsync(() -> {
            HttpHeaders httpHeaders = createHeaders(headers);
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, httpHeaders);

            return restTemplate.execute(url, HttpMethod.POST, request -> {
                request.getHeaders().putAll(entity.getHeaders());
                request.getBody().write(jsonBody.getBytes());
            }, response -> new StreamingHttpResponse(response.getStatusCode().value(), readContent(response), response));
        });
    }

    @Override
    protected CompletableFuture<HttpResponse> executePost(String url, Map<String, String> headers, String fileName,
                                                          InputStream content, String contentType, String partName) {
        return CompletableFuture.supplyAsync(() -> {
            HttpHeaders httpHeaders = createHeaders(headers);
            httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add(partName, new InputStreamResource(content) {
                @Override
                public String getFilename() {
                    return fileName;
                }

                @Override
                public long contentLength() {
                    return -1; // Length unknown
                }
            });

            HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, httpHeaders);
            ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.POST, entity, byte[].class);
            final var bodyData = Optional.ofNullable(response.getBody()).map(ByteArrayInputStream::new).orElse(null);
            return new StreamingHttpResponse(response.getStatusCodeValue(), bodyData, null);
        });
    }

    @Override
    protected CompletableFuture<HttpResponse> executeGet(String url, Map<String, String> headers) {
        return CompletableFuture.supplyAsync(() -> {
            HttpHeaders httpHeaders = createHeaders(headers);
            HttpEntity<?> entity = new HttpEntity<>(httpHeaders);

            return restTemplate.execute(url, HttpMethod.GET, request -> {
                request.getHeaders().putAll(entity.getHeaders());
            }, response -> new StreamingHttpResponse(response.getStatusCode().value(), readContent(response), response));
        });
    }

    @NotNull
    private static InputStream readContent(ClientHttpResponse response) throws IOException {
        try (final var content = response.getBody()) {
            return new ByteArrayInputStream(content.readAllBytes());
        }
    }

    private HttpHeaders createHeaders(Map<String, String> headers) {
        HttpHeaders httpHeaders = new HttpHeaders();
        headers.forEach(httpHeaders::add);
        return httpHeaders;
    }

    @Override
    protected void closeInternal() {
        // RestTemplate doesn't require explicit cleanup
    }
} 
