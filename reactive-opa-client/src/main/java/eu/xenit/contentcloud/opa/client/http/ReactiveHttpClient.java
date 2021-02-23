package eu.xenit.contentcloud.opa.client.http;

import eu.xenit.contentcloud.opa.client.http.mapper.JacksonObjectMapper;
import eu.xenit.contentcloud.opa.client.http.mapper.ObjectMapper;
import java.net.URI;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface ReactiveHttpClient {

    interface Builder<B extends Builder<B>> {

        B connectTimeout(Duration duration);


        /**
         * Set the {@link ObjectMapper ObjectMappers} that should be used with the {@link ReactiveHttpClient}.
         *
         * Setting this value will replace any previously configured object-mappers or replace the default
         * object-mappers on the builder.
         *
         * @param objectMappers the objectMappers to set
         * @return builder
         */
        B objectMappers(Collection<? extends ObjectMapper> objectMappers);

        /**
         * Set the {@link ObjectMapper ObjectMappers} that should be used with the {@link ReactiveHttpClient}
         * to the default set. Calling this method will replace any previously defined converters.
         *
         * @return builder
         */
        default B defaultObjectMappers() {
            return this.objectMappers(List.of(new JacksonObjectMapper()));
        }

        ReactiveHttpClient build();
    }

    RequestSpecification get(URI uri);
    RequestSpecification put(URI uri);
    RequestSpecification post(URI uri);


    default RequestSpecification put(URI uri, Consumer<RequestSpecification> callback) {
        return put(uri).configure(callback);
    }

    default RequestSpecification get(URI uri, Consumer<RequestSpecification> callback) {
        return get(uri).configure(callback);
    }


    interface RequestSpecification {

        RequestSpecification accept(MediaType mediaType);

        <TRequestBody> RequestSpecification body(TRequestBody data);

        URI uri();
        String method();

        @Deprecated
        RequestSpecification configure(Consumer<RequestSpecification> callback);
        ResponseSpecification<Void> response();

        default RequestSpecification accept(String mediaType) {
            return accept(MediaType.parseMediaType(mediaType));
        }
    }


    interface ResponseSpecification<TResponse> {

        <NewTResponse> ResponseSpecification<NewTResponse> body(Class<NewTResponse> type);
        CompletableFuture<ReactiveHttpResponse<TResponse>> execute();
    }


    interface ReactiveHttpResponse<TResponse> {
        TResponse body();
        HttpHeaders headers();
        int statusCode();

//        ReactiveHttpResponse log(Logger logger, boolean body);

        @FunctionalInterface
        interface Logger {
            void log(String format, Object... args);
        }
    }


}
