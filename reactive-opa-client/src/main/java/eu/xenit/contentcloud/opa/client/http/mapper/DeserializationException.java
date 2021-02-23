package eu.xenit.contentcloud.opa.client.http.mapper;

public class DeserializationException extends RuntimeException {

    public DeserializationException(String message) {
        super(message);
    }

    public DeserializationException(Throwable cause) {
        super(cause);
    }

    public DeserializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
