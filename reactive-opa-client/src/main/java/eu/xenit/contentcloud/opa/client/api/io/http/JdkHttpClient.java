package eu.xenit.contentcloud.opa.client.api.io.http;

import eu.xenit.contentcloud.opa.client.api.io.http.ReactiveHttpClient.ReactiveHttpResponse.Logger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public class JdkHttpClient implements ReactiveHttpClient {

    private final HttpClient client;

    private JdkHttpClient(HttpClient httpClient) {
        this.client = httpClient;
    }

    public static JdkHttpClient newClient() {
        return builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

//    @Override
//    public ReactiveHttpRequest get(URI uri) {
//        JdkHttpRequestBodySpec requestSpec = new JdkHttpRequestBodySpec(HttpRequest.newBuilder(uri));
//        callback.accept(requestSpec);
//
//        return new JdkHttpRequest(this.client, requestSpec.build());
//    }

    @Override
    public ReactiveHttpRequest<HttpRequestSpec<?>> get(URI uri) {
        var spec = new JdkHttpRequestSpec<>("GET", uri);
        return new JdkHttpRequest<>(this.client, spec);
    }

    @Override
    public ReactiveHttpRequest<HttpRequestBodySpec> put(URI uri) {
        var requestBuilder = HttpRequest.newBuilder(uri);
        var spec = new JdkHttpRequestBodySpec("PUT", uri);
        return new JdkHttpRequest<>(this.client, spec);
    }

    @Override
    public ReactiveHttpRequest<HttpRequestBodySpec> post(URI uri) {
        var requestBuilder = HttpRequest.newBuilder(uri);
        var spec = new JdkHttpRequestBodySpec("POST", uri);
        return new JdkHttpRequest<>(this.client, spec);
    }

    private static class JdkHttpRequest<TRequestSpec extends HttpRequestSpec<?>> implements
            ReactiveHttpRequest<TRequestSpec> {

        private final HttpClient client;
        private final TRequestSpec requestSpec;

        private JdkHttpRequest(HttpClient client, TRequestSpec requestSpec) {
            Objects.requireNonNull(client, "client cannot be null");
            Objects.requireNonNull(requestSpec, "requestSpec cannot be null");

            this.client = client;
            this.requestSpec = requestSpec;
        }

        @Override
        public ReactiveHttpRequest<TRequestSpec> configure(Consumer<TRequestSpec> callback) {
            callback.accept(this.requestSpec);
            return this;
        }

        @Override
        public CompletableFuture<ReactiveHttpResponse> execute() {
            var request = ((InternalHttpRequestBuilder) this.requestSpec).build();
            return this.client.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                    .thenApply(JdkHttpClientResponse::new);
        }
    }

    private static class JdkHttpClientResponse implements ReactiveHttpResponse {


        private final HttpResponse<byte[]> response;

        private JdkHttpClientResponse(HttpResponse<byte[]> httpResponse) {
            this.response = httpResponse;
        }

        @Override
        public byte[] body() {
            return response.body();
        }

        public HttpHeaders headers() {
            return new JdkHttpHeaders(response.headers());
        }

        @Override
        public ReactiveHttpResponse log(Logger logger, boolean includeBody) {
            String version = response.version() == Version.HTTP_1_1 ? "1.1" : "2";
            logger.log("HTTP/{} {}", version, response.statusCode());


            if (response.statusCode() == 301) {
                logger.log("Location: {}", headers().getLocation().orElse(null));
            } else {
                logger.log("Content-Type: {}", headers().getContentType().orElse(null));

                if (includeBody && response.body().length > 0) {

                    // should we check it makes sense to convert this to text at all ?
                    var content = new String(response.body(), StandardCharsets.UTF_8);
                    Arrays.stream(content.split("\\R")).forEach(logger::log);
                }
            }

            return this;
        }

        private class JdkHttpHeaders implements HttpHeaders {

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


    private static class JdkHttpRequestBodySpec
            extends JdkHttpRequestSpec<HttpRequestBodySpec>
            implements HttpRequestBodySpec {


        JdkHttpRequestBodySpec(String method, URI uri) {
            super(method, uri);
        }

        @Override
        public HttpRequestSpec<?> body(byte[] data) {
            this.request.method(this.method(), HttpRequest.BodyPublishers.ofByteArray(data));
            return self();
        }
    }

    private static class JdkHttpRequestSpec<S extends HttpRequestSpec<S>>
            implements HttpRequestSpec<S>, InternalHttpRequestBuilder {

        protected final HttpRequest.Builder request;

        private final String method;
        private final URI uri;

        JdkHttpRequestSpec(String method, URI uri) {
            this.method = Objects.requireNonNull(method, "method cannot be null");
            this.uri = Objects.requireNonNull(uri, "uri cannot be null");

            this.request = HttpRequest.newBuilder(uri);
        }

        @Override
        public S accept(String... mediaTypes) {
            this.request.header("accept", String.join(",", mediaTypes));
            return self();
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
        public S log(Logger logger, boolean includeBody) {
            logger.log("{} {}", this.method(), this.uri());

            if (includeBody && this)
            // log post body here ?
            return this.self();
        }

        @SuppressWarnings("unchecked")
        protected S self() {
            return (S) this;
        }

        public HttpRequest build() {
            return this.request.build();
        }
    }

    interface InternalHttpRequestBuilder {

        HttpRequest build();
    }

    private static class Builder {

        private Duration connectTimeout;

        public Builder connectTimeout(Duration duration) {
            this.connectTimeout = duration;
            return this;
        }

        public JdkHttpClient build() {
            var inner = HttpClient.newBuilder();

            if (connectTimeout != null) {
                inner.connectTimeout(connectTimeout);
            }
            return new JdkHttpClient(inner.build());

        }
    }
}
