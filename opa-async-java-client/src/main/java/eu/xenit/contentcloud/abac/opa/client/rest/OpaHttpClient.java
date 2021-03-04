package eu.xenit.contentcloud.abac.opa.client.rest;

import eu.xenit.contentcloud.abac.opa.client.rest.http.HttpRequestHeaders;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface OpaHttpClient {

    <TResponse> CompletableFuture<TResponse> get(String path,
            Consumer<HttpRequestHeaders> headersCallback, Class<TResponse> responseType);

    <TRequest, TResponse> CompletableFuture<TResponse> post(
            String path, Consumer<HttpRequestHeaders> headersCallback,
            TRequest requestBody, Class<TResponse> responseType);

    <TRequest, TResponse> CompletableFuture<TResponse> put(
            String path, Consumer<HttpRequestHeaders> headersCallback,
            TRequest requestBody, Class<TResponse> responseType);

    <TResponse> CompletableFuture<TResponse> delete(String path,
            Consumer<HttpRequestHeaders> headersCallback, Class<TResponse> responseType);

    default <T> CompletableFuture<T> get(String path, Class<T> responseType) {
        return this.get(path, null, responseType);
    }

    default <TRequest, TResponse> CompletableFuture<TResponse> post(
            String path, TRequest requestBody, Class<TResponse> responseType) {
        return this.post(path, null, requestBody, responseType);
    }

    default <TRequest, TResponse> CompletableFuture<TResponse> put(
            String path, TRequest requestBody, Class<TResponse> responseType) {
        return this.put(path, null, requestBody, responseType);
    }

    default <TRequest> CompletableFuture<Void> put(String path, TRequest requestBody) {
        return this.put(path, null, requestBody, Void.class);
    }

    default <TResponse> CompletableFuture<TResponse> delete(String path, Class<TResponse> responseType) {
        return this.delete(path, null, responseType);
    }

    void configure(Consumer<RestClientConfiguration> callback);

}
