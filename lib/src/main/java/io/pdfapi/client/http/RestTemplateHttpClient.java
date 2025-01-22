package io.pdfapi.client.http;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

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
            
            ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.POST, entity, byte[].class);
            return new SimpleHttpResponse(response.getStatusCodeValue(), 
                response.getBody() != null ? new String(response.getBody()) : null);
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
            return new SimpleHttpResponse(response.getStatusCodeValue(),
                response.getBody() != null ? new String(response.getBody()) : null);
        });
    }

    @Override
    protected CompletableFuture<HttpResponse> executeGet(String url, Map<String, String> headers) {
        return CompletableFuture.supplyAsync(() -> {
            HttpHeaders httpHeaders = createHeaders(headers);
            HttpEntity<?> entity = new HttpEntity<>(httpHeaders);
            
            ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);
            return new SimpleHttpResponse(response.getStatusCodeValue(),
                response.getBody() != null ? new String(response.getBody()) : null);
        });
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