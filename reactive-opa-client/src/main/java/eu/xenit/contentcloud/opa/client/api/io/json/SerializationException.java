package eu.xenit.contentcloud.opa.client.api.io.json;

public class SerializationException extends RuntimeException {

    public SerializationException(Throwable cause) {
        super(cause);
    }

    public SerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
