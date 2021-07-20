package eu.xenit.contentcloud.thunx.opa.client.rest;

import java.util.function.Consumer;

public interface RestClientConfiguration {

    RestClientConfiguration baseUrl(String baseUrl);

    RestClientConfiguration logging(Consumer<LogSpecification> logging);

    interface LogSpecification {

        LogSpecification none();

        /**
         * Logs everything in the specification, including the request method, url, status code,
         * request- and response-headers, request- and response payload.
         *
         * @return The specification
         */
        LogSpecification all();

        /**
         * Logs the request method, url, status code, response headers and response payload.
         *
         * @return The specification
         */
        LogSpecification verbose();

        LogSpecification requestLine();

        LogSpecification requestHeaders();

        LogSpecification responseHeaders();

        LogSpecification statusCode();

        /**
         * Logs a String-representation of the request body.
         *
         * @return The specification
         */
        LogSpecification requestBody();

        /**
         * Logs a String-representation of the response body.
         *
         * @return The specification
         */
        LogSpecification responseBody();
    }
}
