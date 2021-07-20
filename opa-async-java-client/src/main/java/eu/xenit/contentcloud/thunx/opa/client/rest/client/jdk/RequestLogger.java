package eu.xenit.contentcloud.thunx.opa.client.rest.client.jdk;

import java.io.ByteArrayOutputStream;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.function.BiConsumer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RequestLogger {

    private final RequestLoggerConfiguration config;
    private final BiConsumer<String, Object[]> output;

    public RequestLogger(RequestLoggerConfiguration config, BiConsumer<String, Object[]> output) {
        this.config = config;
        this.output = output;
    }


    public void logRequest(HttpRequest request) {
        if (config.isLogRequestLineEnabled()) {
            output("{} {} HTTP/{}", request.method(), request.uri(),
                    request.version().map(RequestLogger::asString).orElse("?"));
        }

        if (config.isLogRequestHeadersEnabled()) {
            this.printHeaders(request.headers());
        }

        if (config.isLogRequestBodyEnabled() && request.bodyPublisher().isPresent()) {
            var body = request.bodyPublisher().get();

            if (body.contentLength() > 0) {
                var subscriber = new RequestLoggerBodySubscriber(body);
                body.subscribe(subscriber);
            }

        }
    }

    public void logResponse(HttpResponse<byte[]> response, Throwable exception) {
        if (response != null) {
            if (config.isStatusCodeEnabled()) {
                output("HTTP/{} {}", asString(response.version()), response.statusCode());
            }

            if (config.isResponseHeadersEnabled()) {
                this.printHeaders(response.headers());
            }

            if (config.isLogResponseBodyEnabled() && response.statusCode() != 204) {
                // idea for improvement: look if there is a charset encoding ?
                // additionally, what if we need gzip/deflate first ?
                output("<{} bytes>{}{}", response.body().length, System.lineSeparator(), new String(response.body(), StandardCharsets.UTF_8));
            }
        }
    }

    private void printHeaders(HttpHeaders headers) {
        headers.map().forEach((name, values) -> {
            output("  {}: {}", name, String.join(",", values));
        });
    }


    private void output(String format, Object... args) {
        this.output.accept(format, args);
    }

    private static String asString(HttpClient.Version httpVersion) {
        switch (httpVersion) {
            case HTTP_2:
                return "2";
            case HTTP_1_1:
                return "1.1";
            default:
                return "?";
        }
    }

    private class RequestLoggerBodySubscriber implements Subscriber<ByteBuffer> {

        private final BodyPublisher body;
        private final ByteArrayOutputStream outputStream;

        public RequestLoggerBodySubscriber(BodyPublisher body) {
            Objects.requireNonNull(body, "body is required");
            if (body.contentLength() <= 0) {
                throw new IllegalArgumentException("request body is empty, content-length: "+body.contentLength());
            }
            this.body = body;
            this.outputStream = new ByteArrayOutputStream();
        }

        @Override
        public void onSubscribe(Subscription subscription) {
            subscription.request(body.contentLength());
        }

        @Override
        public void onNext(ByteBuffer byteBuffer) {
            outputStream.write(byteBuffer.array(), 0, byteBuffer.limit());
        }

        @Override
        public void onError(Throwable throwable) {
            log.warn("Logging request body failed:", throwable);
        }

        @Override
        public void onComplete() {
            var content = outputStream.toString(StandardCharsets.UTF_8);
            output("<{} bytes>{}{}", outputStream.size(), System.lineSeparator(), content);
        }


    }
}
