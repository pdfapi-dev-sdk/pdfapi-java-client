package io.pdfapi.client.http;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ApacheHttpClient extends AbstractHttpClient {
    private final CloseableHttpClient httpClient;

    public ApacheHttpClient() {
        this.httpClient = HttpClients.createDefault();
    }

    public ApacheHttpClient(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    protected CompletableFuture<HttpResponse> executePost(String url, Map<String, String> headers, String jsonBody) {
        return CompletableFuture.supplyAsync(() -> {
            HttpPost httpPost = new HttpPost(url);
            headers.forEach(httpPost::addHeader);
            httpPost.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                return convertResponse(response);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    protected CompletableFuture<HttpResponse> executePost(String url, Map<String, String> headers, String fileName,
                                                          InputStream content, String contentType, String partName) {
        return CompletableFuture.supplyAsync(() -> {
            HttpPost httpPost = new HttpPost(url);
            headers.forEach(httpPost::addHeader);

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addBinaryBody(partName, content, ContentType.create(contentType), fileName);
            httpPost.setEntity(builder.build());

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                return convertResponse(response);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    protected CompletableFuture<HttpResponse> executeGet(String url, Map<String, String> headers) {
        return CompletableFuture.supplyAsync(() -> {
            HttpGet httpGet = new HttpGet(url);
            headers.forEach(httpGet::addHeader);

            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                return convertResponse(response);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private HttpResponse convertResponse(CloseableHttpResponse response) throws IOException {
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            return new StreamingHttpResponse(response.getStatusLine().getStatusCode(), readContent(entity), response, mapHeaders(response.getAllHeaders()));
        } else {
            response.close();
            return new StreamingHttpResponse(response.getStatusLine().getStatusCode(), null, null, mapHeaders(response.getAllHeaders()));
        }
    }

    private Map<String, List<String>> mapHeaders(Header[] allHeaders) {
        return Arrays.stream(allHeaders)
                .collect(Collectors.groupingBy(
                        Header::getName,
                        Collectors.mapping(Header::getValue, Collectors.toList())
                ));
    }

    private static InputStream readContent(HttpEntity entity) throws IOException {
        try (final var content = entity.getContent()) {
            return new ByteArrayInputStream(content.readAllBytes());
        }
    }

    @Override
    protected void closeInternal() {
        try {
            httpClient.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to close Apache HTTP client", e);
        }
    }
} 
