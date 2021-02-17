package eu.xenit.contentcloud.opa.client.api.io.http;

import eu.xenit.contentcloud.opa.client.api.io.http.ReactiveHttpClient.ReactiveHttpResponse.Logger;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface ReactiveHttpClient {

    ReactiveHttpRequest<HttpRequestSpec<?>> get(URI uri);
    ReactiveHttpRequest<HttpRequestBodySpec> put(URI uri);
    ReactiveHttpRequest<HttpRequestBodySpec> post(URI uri);


    default ReactiveHttpRequest<HttpRequestBodySpec> put(URI uri, Consumer<HttpRequestBodySpec> callback) {
        return put(uri).configure(callback);
    }

    default ReactiveHttpRequest<HttpRequestSpec<?>> get(URI uri, Consumer<HttpRequestSpec<?>> callback) {
        return get(uri).configure(callback);
    }


    interface HttpRequestBodySpec extends HttpRequestSpec<HttpRequestBodySpec> {

        HttpRequestSpec<?> body(byte[] data);
//        <T, P extends CompletableFuture<T>> HttpRequestSpec<?> body(P publisher, Class<T> type);
    }


//    interface HttpRequestHeadersSpec extends HttpRequestSpec<HttpRequestHeadersSpec> {
//
//    }

    interface HttpRequestSpec<S extends HttpRequestSpec<S>> {
        S accept(String... mediaTypes);
//
        URI uri();
        String method();

        S log(Logger logger, boolean body);
    }

    interface ReactiveHttpRequest<TSpec> {

        ReactiveHttpRequest<TSpec> configure(Consumer<TSpec> callback);

        CompletableFuture<ReactiveHttpResponse> execute();

    }

//    interface ReactiveHttpResponseSpec {
////        byte[] body();
//
//        <T> CompletableFuture<HttpResponse<T>> toResponse(Class<T> type);
//        <T> CompletableFuture<T> toBody(Class<T> type);
//
//        CompletableFuture<HttpResponse<Void>> ignoreBodyResponse();
//    }

    interface ReactiveHttpResponse {
        byte[] body();

        ReactiveHttpResponse log(Logger logger, boolean body);

        @FunctionalInterface
        interface Logger {
            void log(String format, Object... args);
        }
    }


}
