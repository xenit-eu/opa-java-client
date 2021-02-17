package eu.xenit.contentcloud.opa.client.api.io.json;

public class DeserializationException extends RuntimeException {

    public DeserializationException(Throwable cause) {
        super(cause);
    }

    public DeserializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
