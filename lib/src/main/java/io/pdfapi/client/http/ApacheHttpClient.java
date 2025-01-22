package io.pdfapi.client.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

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
            InputStream content, String contentType) {
        return CompletableFuture.supplyAsync(() -> {
            HttpPost httpPost = new HttpPost(url);
            headers.forEach(httpPost::addHeader);
            httpPost.setEntity(new InputStreamEntity(content, ContentType.create(contentType)));

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
        String body = entity != null ? EntityUtils.toString(entity) : null;
        return new SimpleHttpResponse(response.getStatusLine().getStatusCode(), body);
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