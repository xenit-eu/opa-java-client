package eu.xenit.contentcloud.opa.client.rest.client.jdk;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class RequestLoggerConfiguration {

    private final Set<LogDetail> details = new HashSet<>();

    public RequestLoggerConfiguration(Collection<LogDetail> details) {
        this.details.addAll(details);
    }

    public boolean isLogRequestLineEnabled() {
        return this.details.contains(LogDetail.REQUEST_LINE);
    }

    public boolean isStatusCodeEnabled() {
        return this.details.contains(LogDetail.RESPONSE_STATUS);
    }

    public boolean isResponseHeadersEnabled() {
        return this.details.contains(LogDetail.RESPONSE_HEADERS);
    }

    public boolean isLogResponseBodyEnabled() {
        return this.details.contains(LogDetail.RESPONSE_BODY);
    }

    public boolean isLogRequestHeadersEnabled() {
        return this.details.contains(LogDetail.REQUEST_HEADERS);
    }

    public boolean isLogRequestBodyEnabled() {
        return this.details.contains(LogDetail.REQUEST_BODY);
    }

    public enum LogDetail {
        /**
         * Log the request headers
         */
        REQUEST_HEADERS,
        /**
         * Log the response headers
         */
        RESPONSE_HEADERS,

        /**
         * Log the cookies
         */
        COOKIES,
        /**
         * Log the request body.
         */
        REQUEST_BODY,
        /**
         * Log the response body.
         */
        RESPONSE_BODY,
        /**
         * Log the status code.
         */
        RESPONSE_STATUS,
        /**
         * Logs the request method and URI.
         */
        REQUEST_LINE,
    }
}
