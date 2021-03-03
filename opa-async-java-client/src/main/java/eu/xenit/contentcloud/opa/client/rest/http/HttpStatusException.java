package eu.xenit.contentcloud.opa.client.rest.http;

public class HttpStatusException extends RuntimeException {

    private final int statusCode;

    public HttpStatusException(int statusCode) {
        super("HTTP "+statusCode);
        this.statusCode = statusCode;
    }

    public int statusCode() {
        return statusCode;
    }
}
