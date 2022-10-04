package com.contentgrid.opa.client.rest.client.jdk;

import com.contentgrid.opa.client.rest.OpaHttpClient;
import com.contentgrid.opa.client.rest.client.jdk.converter.ConverterProcessor;
import com.contentgrid.opa.client.rest.client.jdk.converter.HttpBodyConverter.DeserializationContext;
import com.contentgrid.opa.client.rest.client.jdk.converter.HttpBodyConverter.SerializationContext;
import com.contentgrid.opa.client.rest.client.jdk.converter.JacksonBodyConverter;
import com.contentgrid.opa.client.rest.client.jdk.converter.StringConverter;
import com.contentgrid.opa.client.rest.http.HttpRequestHeaders;
import com.contentgrid.opa.client.rest.http.HttpStatusException;
import com.contentgrid.opa.client.rest.http.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.contentgrid.opa.client.rest.RestClientConfiguration;
import com.contentgrid.opa.client.rest.http.HttpMethod;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultOpaHttpClient implements OpaHttpClient {

    private final HttpClient httpClient;

    final ConverterProcessor converterProcessor;

    private URI baseUrl;
    private RequestLogger logger;

    private static final String MEDIA_APPLICATION_JSON = "application/json";
    private static final String MEDIA_TEXT_PLAIN_CHARSET_UTF8 = "text/plain; charset=utf-8";


    private static final String HEADER_ACCEPT = "Accept";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";

    public DefaultOpaHttpClient(HttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;

        this.converterProcessor = new ConverterProcessor(List.of(
                new StringConverter(),
                new JacksonBodyConverter(objectMapper)
        ));
    }


    private URI createUri(String path) {
        return this.baseUrl.resolve(path);
    }

    @Override
    public <TResponse> CompletableFuture<TResponse> get(String path, Consumer<HttpRequestHeaders> headersCallback,
            Class<TResponse> responseType) {
        return this.execute(HttpMethod.GET, this.createUri(path), headersCallback, null, responseType);
    }

    @Override
    public <TRequest, TResponse> CompletableFuture<TResponse> post(String path,
            Consumer<HttpRequestHeaders> headersCallback, TRequest requestBody, Class<TResponse> responseType) {
        return this.execute(HttpMethod.POST, this.createUri(path), headersCallback, requestBody, responseType);
    }

    @Override
    public <TRequest, TResponse> CompletableFuture<TResponse> put(String path,
            Consumer<HttpRequestHeaders> headersCallback, TRequest requestBody, Class<TResponse> responseType) {
        return this.execute(HttpMethod.PUT, this.createUri(path), headersCallback, requestBody, responseType);
    }

    @Override
    public <TResponse> CompletableFuture<TResponse> delete(String path, Consumer<HttpRequestHeaders> headersCallback,
            Class<TResponse> responseType) {
        return this.execute(HttpMethod.DELETE, this.createUri(path), headersCallback, null, responseType);
    }

    @Override
    public void configure(Consumer<RestClientConfiguration> callback) {
        var config = new DefaultRestClientConfiguration(
                uri -> this.baseUrl = uri,
                logConfig -> this.logger = new RequestLogger(logConfig, log::info)
        );

        callback.accept(config);
    }


    private <TRequest, TResponse> CompletableFuture<TResponse> execute(
            HttpMethod method, URI uri,
            Consumer<HttpRequestHeaders> headersCallback,
            TRequest requestBody, Class<TResponse> responseType) {

        HttpHeadersBuilder headers = createDefaultRequestHeaders();
        if (headersCallback != null) {
            headersCallback.accept(headers);
        }

        return CompletableFuture.completedFuture(SerializationContext.of(requestBody, headers))
                .thenApplyAsync(this.converterProcessor::write)
                .thenApply(httpEntity -> {
                    var request = HttpRequest.newBuilder(uri);

                    // convert httpEntity in proper request
                    // 1. apply headers
                    httpEntity.getHeaders().forEach((key, values) -> {
                        values.forEach(val -> {
                            request.header(key, val);
                        });
                    });

                    // 2. apply optional payload
                    var requestBodyPublisher = httpEntity.getBody().map(BodyPublishers::ofByteArray)
                            .orElse(BodyPublishers.noBody());
                    request.method(method.toString(), requestBodyPublisher);

                    return request;
                })
                .thenApply(HttpRequest.Builder::build)
                .thenApply(this::logRequest)
                .thenCompose(request -> this.httpClient.sendAsync(request, BodyHandlers.ofByteArray()))
                .whenComplete(this::logResponse)
                .whenComplete(this::handleStatusCode)
                .thenApply(response -> {
                    var responseContentType = response.headers().firstValue("Content-Type")
                            .map(MediaType::parseMediaType)
                            .orElse(null);
                    var context = DeserializationContext.of(response.body(), responseContentType);
                    return this.converterProcessor.read(context, responseType);
                });
    }


    private void handleStatusCode(HttpResponse<byte[]> response, Throwable exception) {
        if (response != null && response.statusCode() >= 400) {
            throw new HttpStatusException(response.statusCode());
        }
    }

    protected HttpHeadersBuilder createDefaultRequestHeaders() {
        var headers = new HttpHeadersBuilder();
        headers.contentType(MediaType.APPLICATION_JSON);
        headers.accept(MediaType.APPLICATION_JSON);
        return headers;
    }

    protected HttpRequest logRequest(HttpRequest request) {
        logger.logRequest(request);
        return request;
    }

    protected void logResponse(HttpResponse<byte[]> response, Throwable exception) {
        logger.logResponse(response, exception);
    }

}
