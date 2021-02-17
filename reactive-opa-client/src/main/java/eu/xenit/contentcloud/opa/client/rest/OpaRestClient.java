package eu.xenit.contentcloud.opa.client.rest;

import eu.xenit.contentcloud.opa.client.OpaConfiguration;
import eu.xenit.contentcloud.opa.client.api.io.http.ReactiveHttpClient;
import eu.xenit.contentcloud.opa.client.api.io.http.ReactiveHttpClient.HttpRequestBodySpec;
import eu.xenit.contentcloud.opa.client.api.io.http.ReactiveHttpClient.ReactiveHttpResponse;
import eu.xenit.contentcloud.opa.client.api.io.json.JsonObjectMapper;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OpaRestClient implements AutoCloseable {

    private final OpaConfiguration configuration;
    private final ReactiveHttpClient httpClient;
    private final JsonObjectMapper jsonMapper;

    private final List<RequestListener> requestListeners;
    private final List<ResponseListener> responseListeners;

    public OpaRestClient(OpaConfiguration configuration, ReactiveHttpClient httpClient, JsonObjectMapper jsonMapper) {
        this(configuration, httpClient, jsonMapper, Collections.emptyList(), Collections.emptyList());
    }

    private static final String MEDIA_APPLICATION_JSON = "application/json";

    private URI resolve(String path) {
        return URI.create(this.configuration.getUrl()).resolve(path);
    }

    public <T> CompletableFuture<T> get(String path, Class<T> responseType) {
        return this.httpClient
                .get(this.resolve(path))
                .configure(request -> request.accept(MEDIA_APPLICATION_JSON))
                .execute()
                .whenCompleteAsync(this::handleOnResponse)
                .thenApply(response -> jsonMapper.deserialize(response.body(), responseType));
    }

    public <TRequest, TResponse> CompletableFuture<TResponse> put(String path, TRequest object,
            Class<TResponse> responseType) {
        return this.httpClient
                .put(this.resolve(path))
                .configure(request -> request.accept(MEDIA_APPLICATION_JSON).body(this.jsonMapper.serialize(object)))
                .execute()
                .whenCompleteAsync(this::handleOnResponse)
                .thenApply(response -> jsonMapper.deserialize(response.body(), responseType));
    }

    public <TRequest> CompletableFuture<Void> put(String path, TRequest object) {
        return this.httpClient
                .put(this.resolve(path))
                .configure(request -> request.accept(MEDIA_APPLICATION_JSON).body(this.jsonMapper.serialize(object)))
                .configure(this::handleOnRequest)
                .execute()
                .whenCompleteAsync(this::handleOnResponse)
                .thenAccept(response -> {
                    // check status code, expecting 204 ?
                });
    }


    public <TRequest, TResponse> CompletableFuture<TResponse> post(String path, TRequest object,
            Class<TResponse> responseType) {
        return this.httpClient
                .post(this.resolve(path))
                .configure(request -> request.accept(MEDIA_APPLICATION_JSON).body(this.jsonMapper.serialize(object)))
                .configure(this::handleOnRequest)
                .execute()
                .whenCompleteAsync(this::handleOnResponse)
                .thenApply(response -> jsonMapper.deserialize(response.body(), responseType));
    }

    private void handleOnRequest(HttpRequestBodySpec request) {
        this.requestListeners.forEach(listener -> listener.accept(request));
    }

    private void handleOnResponse(ReactiveHttpResponse response, Throwable exception) {
        if (exception == null) {
            this.responseListeners.forEach(listener -> listener.accept(response));
        }
    }

    @Override
    public void close() {
        this.responseListeners.clear();
        this.requestListeners.clear();
    }
}
