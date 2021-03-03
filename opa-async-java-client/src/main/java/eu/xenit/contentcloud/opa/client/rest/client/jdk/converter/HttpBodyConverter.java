package eu.xenit.contentcloud.opa.client.rest.client.jdk.converter;

import eu.xenit.contentcloud.opa.client.rest.http.HttpRequestHeaders;
import eu.xenit.contentcloud.opa.client.rest.http.MediaType;
import java.util.Collection;
import java.util.Objects;

/**
 * An object mapper is used to serialize and deserialize a Java object to and from a String, byte[] or InputStream.
 */
public interface HttpBodyConverter {

    <T> boolean canRead(DeserializationContext context, Class<T> type);
    <T> T read(DeserializationContext context, Class<T> type);

    boolean canWrite(SerializationContext context);
    byte[] write(SerializationContext context);

    Collection<MediaType> getSupportedMediaTypes();

    /**
     *
     * Checks if this {@link HttpBodyConverter} supports the provided media type.
     *
     * Used for both reading and writing.
     *
     * @param mediaType to convert from or into
     * @return {@code true} if mediaType is null or is compatible, otherwise {@code false}
     */
    default boolean supportsMediaType(MediaType mediaType) {
        if (mediaType == null) {
            return true;
        }

        for (MediaType supportedMediaType : getSupportedMediaTypes()) {
            if (supportedMediaType.includes(mediaType)) {
                return true;
            }
        }
        return false;
    }


    interface DeserializationContext {

        /**
         * @return The content type of the response
         */
        MediaType getContentType();

        byte[] getSource();

        static DeserializationContext of(byte[] source, MediaType contentType) {
            return new DeserializationContext() {
                @Override
                public MediaType getContentType() {
                    return contentType;
                }

                @Override
                public byte[] getSource() {
                    return source;
                }
            };
        }
    }

    interface SerializationContext {

        /**
         * @return The object to serialize
         */
        Object getSource();

        /**
         * @return The http request headers
         */
        HttpRequestHeaders getHeaders();

        /**
         * @return The target content type of the request, possibly {@code null}
         */
        default MediaType getContentType() {
            return this.getHeaders().contentType().orElse(null);
        }


        default boolean isEmpty() {
            return Objects.isNull(this.getSource());
        }

        static SerializationContext of(Object source, HttpRequestHeaders requestHeaders) {
            return new SerializationContext() {

                @Override
                public Object getSource() {
                    return source;
                }

                @Override
                public HttpRequestHeaders getHeaders() {
                    return requestHeaders;
                }
            };
        }
    }
}
