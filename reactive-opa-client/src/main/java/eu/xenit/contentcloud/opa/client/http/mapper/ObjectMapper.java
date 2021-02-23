package eu.xenit.contentcloud.opa.client.http.mapper;

import eu.xenit.contentcloud.opa.client.http.MediaType;

/**
 * An object mapper is used to serialize and deserialize a Java object to and from a String, byte[] or InputStream.
 */
public interface ObjectMapper {

    <T> boolean canRead(DeserializationContext context, Class<T> type);
    <T> T read(DeserializationContext context, Class<T> type) throws DeserializationException;

    boolean canWrite(SerializationContext context);
    byte[] write(SerializationContext context) throws SerializationException;


    interface DeserializationContext {

        /**
         * @return The content type of the response
         */
        MediaType getContentType();

        byte[] getSource();
    }

    interface SerializationContext {

        /**
         * @return The object to serialize
         */
        Object getSource();

        /**
         * @return The target content type of the request
         */
        MediaType getContentType();


    }
}
