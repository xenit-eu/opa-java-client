package eu.xenit.contentcloud.abac.opa.client.rest.client.jdk;

import eu.xenit.contentcloud.abac.opa.client.rest.RestClientConfiguration;
import eu.xenit.contentcloud.abac.opa.client.rest.client.jdk.RequestLoggerConfiguration.LogDetail;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

class DefaultRestClientConfiguration implements RestClientConfiguration {

    private final Consumer<URI> uriCallback;
    private final Consumer<RequestLoggerConfiguration> logConfig;

    DefaultRestClientConfiguration(Consumer<URI> uriConfig, Consumer<RequestLoggerConfiguration> logConfig) {
        this.uriCallback = uriConfig;
        this.logConfig = logConfig;
    }

    @Override
    public RestClientConfiguration baseUrl(String baseUrl) {
        this.uriCallback.accept(URI.create(baseUrl));
        return this;
    }

    @Override
    public RestClientConfiguration logging(Consumer<LogSpecification> callback) {
        var logSpec = new DefaultLogSpecification();
        callback.accept(logSpec);

        this.logConfig.accept(logSpec.build());

        return this;
    }

    static class DefaultLogSpecification implements LogSpecification {

        private final Set<LogDetail> details = new HashSet<>();

        @Override
        public LogSpecification none() {
            this.details.clear();
            return this;
        }

        @Override
        public LogSpecification all() {
            return this.verbose().requestBody().requestHeaders();
        }

        @Override
        public LogSpecification verbose() {
            return this.requestLine().statusCode().responseHeaders().responseBody();
        }

        @Override
        public LogSpecification requestLine() {
            this.details.add(RequestLoggerConfiguration.LogDetail.REQUEST_LINE);
            return this;
        }

        @Override
        public LogSpecification requestHeaders() {
            this.details.add(RequestLoggerConfiguration.LogDetail.REQUEST_HEADERS);
            return this;
        }

        @Override
        public LogSpecification responseHeaders() {
            this.details.add(RequestLoggerConfiguration.LogDetail.RESPONSE_HEADERS);
            return this;
        }

        @Override
        public LogSpecification statusCode() {
            this.details.add(RequestLoggerConfiguration.LogDetail.RESPONSE_STATUS);
            return this;
        }

        @Override
        public LogSpecification requestBody() {
            this.details.add(RequestLoggerConfiguration.LogDetail.REQUEST_BODY);
            return this;
        }

        @Override
        public LogSpecification responseBody() {
            this.details.add(RequestLoggerConfiguration.LogDetail.RESPONSE_BODY);
            return this;
        }

        RequestLoggerConfiguration build() {
            return new RequestLoggerConfiguration(details);
        }
    }
}
