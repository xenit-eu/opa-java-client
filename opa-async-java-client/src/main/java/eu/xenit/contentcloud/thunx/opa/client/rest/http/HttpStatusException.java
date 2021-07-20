package eu.xenit.contentcloud.thunx.opa.client.rest.http;

import lombok.Getter;

public class HttpStatusException extends RuntimeException {

    @Getter
    private final int statusCode;

    public HttpStatusException(int statusCode) {
        super("HTTP "+statusCode);
        this.statusCode = statusCode;
    }

    public int statusCode() {
        return statusCode;
    }
}
