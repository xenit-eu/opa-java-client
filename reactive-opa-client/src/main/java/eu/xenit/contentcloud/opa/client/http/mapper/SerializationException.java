package eu.xenit.contentcloud.opa.client.http.mapper;

public class SerializationException extends RuntimeException {

    public SerializationException(Throwable cause) {
        super(cause);
    }

    public SerializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public SerializationException(String msg) {
        super(msg);
    }
}
