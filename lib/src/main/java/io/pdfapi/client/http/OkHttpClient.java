package io.pdfapi.client.http;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class OkHttpClient extends AbstractHttpClient {
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final okhttp3.OkHttpClient client;

    public OkHttpClient(int timeoutSeconds) {
        this.client = new okhttp3.OkHttpClient.Builder()
                .connectTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .readTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .writeTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .build();
    }

    public OkHttpClient(okhttp3.OkHttpClient client) {
        this.client = client;
    }

    @Override
    protected CompletableFuture<HttpResponse> executePost(String url, Map<String, String> headers, String jsonBody) {
        Request request = new Request.Builder()
                .url(url)
                .headers(Headers.of(headers))
                .post(RequestBody.create(jsonBody, JSON))
                .build();

        return executeAsync(request);
    }

    @Override
    protected CompletableFuture<HttpResponse> executePost(String url, Map<String, String> headers, String fileName,
                                                          InputStream content, String contentType, String partName) {
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(partName, fileName,
                        new InputStreamRequestBody(MediaType.get(contentType), content))
                .build();

        Request request = new Request.Builder()
                .url(url)
                .headers(Headers.of(headers))
                .post(requestBody)
                .build();

        return executeAsync(request);
    }

    @Override
    protected CompletableFuture<HttpResponse> executeGet(String url, Map<String, String> headers) {
        Request request = new Request.Builder()
                .url(url)
                .headers(Headers.of(headers))
                .get()
                .build();

        return executeAsync(request);
    }

    private CompletableFuture<HttpResponse> executeAsync(Request request) {
        CompletableFuture<HttpResponse> future = new CompletableFuture<>();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(Call call, Response response) {
                ResponseBody body = response.body();
                if (body != null) {
                    future.complete(new StreamingHttpResponse(response.code(), body.byteStream(), response, response.headers().toMultimap()));
                } else {
                    response.close();
                    future.complete(new StreamingHttpResponse(response.code(), null, null, response.headers().toMultimap()));
                }
            }
        });
        return future;
    }

    @Override
    protected void closeInternal() {
        client.dispatcher().executorService().shutdown();
        client.connectionPool().evictAll();
    }

    private static class InputStreamRequestBody extends RequestBody {
        private final MediaType contentType;
        private final InputStream inputStream;

        InputStreamRequestBody(MediaType contentType, InputStream inputStream) {
            this.contentType = contentType;
            this.inputStream = inputStream;
        }

        @Override
        public MediaType contentType() {
            return contentType;
        }

        @Override
        public void writeTo(okio.BufferedSink sink) throws IOException {
            try (okio.Source source = okio.Okio.source(inputStream)) {
                sink.writeAll(source);
            }
        }
    }
} 
