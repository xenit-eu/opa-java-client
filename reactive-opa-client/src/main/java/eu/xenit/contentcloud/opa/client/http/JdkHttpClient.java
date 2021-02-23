package eu.xenit.contentcloud.opa.client.http;

import eu.xenit.contentcloud.opa.client.http.mapper.JacksonObjectMapper;
import eu.xenit.contentcloud.opa.client.http.mapper.ObjectMapper;
import eu.xenit.contentcloud.opa.client.http.mapper.ObjectMapper.DeserializationContext;
import eu.xenit.contentcloud.opa.client.http.mapper.ObjectMapper.SerializationContext;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodySubscriber;
import java.net.http.HttpResponse.ResponseInfo;
import java.time.Duration;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class JdkHttpClient implements ReactiveHttpClient {

    private final HttpClient client;
    private final ObjectMapping objectMapping;

    private JdkHttpClient(HttpClient httpClient, Set<ObjectMapper> objectMappers) {
        this.client = httpClient;
        this.objectMapping = new ObjectMapping(objectMappers);
    }

    public static JdkHttpClient newClient() {
        return builder().build();
    }

    public static JdkHttpClientBuilder builder() {
        return new JdkHttpClientBuilder();
    }

    @Override
    public RequestSpecification get(URI uri) {
        return new JdkHttpRequestSpecification(this.client, "GET", uri, this.objectMapping);
    }

    @Override
    public RequestSpecification put(URI uri) {

//        var spec = new JdkHttpRequestBodySpec("PUT", uri);
        return new JdkHttpRequestSpecification(this.client, "PUT", uri, this.objectMapping);
    }

    @Override
    public RequestSpecification post(URI uri) {
//        var requestBuilder = HttpRequest.newBuilder(uri);
//        var spec = new JdkHttpRequestBodySpec("POST", uri);
        return new JdkHttpRequestSpecification(this.client, "POST", uri, this.objectMapping);
    }

    private static class JdkHttpRequestSpecification implements RequestSpecification {

        private final HttpClient client;
        private final String method;
        private final URI uri;

        private final HttpRequest.Builder builder;
        private final ObjectMapping objectMapping;

        private HttpRequest.BodyPublisher bodyHandler = HttpRequest.BodyPublishers.noBody();

        private MediaType _cachedAcceptMediaType;

        private JdkHttpRequestSpecification(HttpClient client, String method, URI uri, ObjectMapping objectMapping) {
            this.client = Objects.requireNonNull(client, "client cannot be null");
            this.method = Objects.requireNonNull(method, "method cannot be null");
            this.uri = Objects.requireNonNull(uri, "uri cannot be null");
            this.objectMapping = Objects.requireNonNull(objectMapping, "objectMapping cannot be null");

            this.builder = HttpRequest.newBuilder(uri);

        }

        @Override
        public RequestSpecification accept(MediaType mediaType) {
            this._cachedAcceptMediaType = mediaType;
            this.builder.header(HttpHeaders.HeaderNames.ACCEPT, mediaType.toString());
            return this;
        }

        public MediaType accept() {
            return this._cachedAcceptMediaType;
        }

        @Override
        public <TRequestBody> RequestSpecification body(TRequestBody data) {
            var bytes = this.objectMapping.write(new JdkHttpClientSerializationContext(data, this::accept));
            this.bodyHandler = HttpRequest.BodyPublishers.ofByteArray(bytes);
            return this;
        }

        @Override
        public URI uri() {
            return this.uri;
        }

        @Override
        public String method() {
            return this.method;
        }

        @Override
        public RequestSpecification configure(Consumer<RequestSpecification> callback) {
            callback.accept(this);
            return this;
        }

        @Override
        public ResponseSpecification<Void> response() {

            final HttpRequest request = this.builder.method(this.method, this.bodyHandler).build();

            return new JdkHttpClientResponseSpec<>(this.client, request, HttpResponse.BodyHandlers.discarding(), this.objectMapping);
        }

//        @Override
//        public CompletableFuture<ReactiveHttpResponse> execute() {
//            var request = ((InternalHttpRequestBuilder) this.requestSpec).build();
//            return this.client.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
//                    .thenApply(JdkHttpClientResponse::new);
//        }
    }

    @RequiredArgsConstructor
    private static class JdkHttpClientSerializationContext implements SerializationContext {
        @Getter
        private final Object source;
        private final Supplier<MediaType> mediaTypeSupplier;

        @Override
        public MediaType getContentType() {
            return this.mediaTypeSupplier.get();
        }
    }

    private static class JdkHttpClientResponseSpec<TResponse>
            implements ResponseSpecification<TResponse> {

        private final HttpClient client;
        private final HttpRequest request;
        private final HttpResponse.BodyHandler<TResponse> bodyHandler;
        private final ObjectMapping objectMapping;

        private JdkHttpClientResponseSpec(HttpClient client, HttpRequest request,
                BodyHandler<TResponse> bodyHandler, ObjectMapping objectMapping) {
            this.client = client;
            this.request = request;
            this.bodyHandler = bodyHandler;
            this.objectMapping = objectMapping;
        }

        @Override
        public <NewResponse> ResponseSpecification<NewResponse> body(Class<NewResponse> type) {
            return new JdkHttpClientResponseSpec<>(this.client, this.request, new HttpResponse.BodyHandler<NewResponse>() {

                @Override
                public BodySubscriber<NewResponse> apply(ResponseInfo responseInfo) {
                    HttpResponse.BodySubscriber<byte[]> upstream = HttpResponse.BodySubscribers.ofByteArray();
                    return HttpResponse.BodySubscribers.mapping(upstream, (byteArray) -> {
                        var context = new JdkResponseInfoDeserializationContext(responseInfo, byteArray);
                        return objectMapping.read(context, type);
                    });
                }
            }, this.objectMapping);
        }

        @Override
        public CompletableFuture<ReactiveHttpResponse<TResponse>> execute() {
            return this.client.sendAsync(request, bodyHandler)
                    .thenApply(JdkHttpClientResponse::new);
        }

        private static class JdkResponseInfoDeserializationContext implements DeserializationContext {

            private final ResponseInfo responseInfo;
            private final byte[] body;

            public JdkResponseInfoDeserializationContext(ResponseInfo responseInfo, byte[] body) {
                this.responseInfo = responseInfo;
                this.body = body;
            }

            @Override
            public MediaType getContentType() {
                return this.responseInfo.headers()
                        .firstValue(HttpHeaders.HeaderNames.CONTENT_TYPE)
                        .map(MediaType::parseMediaType)
                        .orElse(null);
            }

            @Override
            public byte[] getSource() {
                return this.body;
            }
        }
    }

    private static class JdkHttpClientResponse<TResponse> implements ReactiveHttpResponse<TResponse> {


        private final HttpResponse<TResponse> response;

        private JdkHttpClientResponse(HttpResponse<TResponse> httpResponse) {
            this.response = httpResponse;
        }

        @Override
        public TResponse body() {
            return response.body();
        }

        public HttpHeaders headers() {
            return new JdkHttpHeaders(response.headers());
        }

        @Override
        public int statusCode() {
            return this.response.statusCode();
        }

        private static class JdkHttpHeaders implements HttpHeaders {

            private final java.net.http.HttpHeaders headers;

            public JdkHttpHeaders(java.net.http.HttpHeaders headers) {
                Objects.requireNonNull(headers, "headers cannot be null");
                this.headers = headers;
            }

            @Override
            public List<String> get(String headerName) {
                Objects.requireNonNull(headerName, "headerName cannot be null");
                return this.headers.allValues(headerName);
            }
        }
    }

    private static class JdkHttpClientBuilder implements Builder<JdkHttpClientBuilder> {

        private Duration connectTimeout;

        private final Set<ObjectMapper> objectMappers = new LinkedHashSet<>();

        private JdkHttpClientBuilder() {
            this.defaultObjectMappers();
        }

        public JdkHttpClientBuilder connectTimeout(Duration duration) {
            this.connectTimeout = duration;
            return this;
        }

        @Override
        public JdkHttpClientBuilder objectMappers(Collection<? extends ObjectMapper> objectMappers) {
            this.objectMappers.clear();
            this.objectMappers.addAll(objectMappers);
            return this;
        }




        public JdkHttpClient build() {
            var inner = HttpClient.newBuilder();

            if (connectTimeout != null) {
                inner.connectTimeout(connectTimeout);
            }

            inner.followRedirects(Redirect.NORMAL);

            return new JdkHttpClient(inner.build(), objectMappers);

        }
    }
}
