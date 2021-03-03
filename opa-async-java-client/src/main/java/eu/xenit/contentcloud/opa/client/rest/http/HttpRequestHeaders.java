package eu.xenit.contentcloud.opa.client.rest.http;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface HttpRequestHeaders {

    final class Headers {

        private Headers() {
        }

        public static final String LOCATION = "Location";
        public static final String CONTENT_TYPE = "Content-Type";
        public static final String ACCEPT = "Accept";
        public static final String ACCEPT_CHARSET = "Accept-Charset";

    }

    /**
     * Adds the given name value pair to the set of headers for this request. The given value is added to the list of
     * values for that name.
     *
     * @param headerName the header name
     * @param headerValue the header value
     * @return this instance
     */
    HttpRequestHeaders add(String headerName, String headerValue);

    /**
     * Sets the given name value pair to the set of headers for this request.
     *
     * @param headerName the header name
     * @param headerValues the header values
     * @return this instance
     */
    HttpRequestHeaders set(String headerName, String... headerValues);

    /**
     * Removes the header with the specified name, if present.
     *
     * @param headerName the header name
     * @return this instance
     */
    HttpRequestHeaders remove(String headerName);

    /**
     * Get an immutable list of header values for the given header name, possibly empty.
     *
     * @param headerName the header name
     * @return the list of header values, or an empty list
     */
    List<String> get(String headerName);

    /**
     * Returns an {@linkplain Optional} of the first header value for the given header name, if any.
     *
     * @param headerName the header name
     * @return an {@code Optional} of first header value or {@code Optional.empty()} if none
     */
    default Optional<String> getFirst(String headerName) {
        return this.get(headerName).stream().findFirst();
    }


    /**
     * Returns an {@linkplain Optional} of {@link MediaType media type} of the body, as specified by the {@code
     * Content-Type} header.
     *
     * @return an {@link Optional} of the of the {@link MediaType Content-Type} header
     */
    default Optional<MediaType> contentType() {
        return getFirst(Headers.CONTENT_TYPE).map(str -> {
            if (str.isEmpty()) {
                return null;
            }

            return MediaType.parseMediaType(str);
        }).or(Optional::empty);
    }

    /**
     * Sets the {@linkplain MediaType} of the body, specified by the {@code Content-Type} header.
     *
     * Can be null.
     *
     * @return this instance
     */
    default HttpRequestHeaders contentType(MediaType contentType) {
        if (contentType != null) {
            this.set(Headers.CONTENT_TYPE, contentType.toString());
        } else {
            this.remove(Headers.CONTENT_TYPE);
        }
        return this;
    }

    /**
     * Set the list of acceptable {@linkplain MediaType media types}, as specified by the {@code Accept} header.
     *
     * @param acceptableMediaTypes the acceptable media types
     * @return this instance
     */
    default HttpRequestHeaders accept(MediaType... acceptableMediaTypes) {
        return this.set(
                Headers.ACCEPT,
                Stream.of(acceptableMediaTypes)
                        .map(MediaType::toString)
                        .collect(Collectors.joining(", ")));
    }

    /**
     * Set the list of acceptable {@linkplain Charset charsets}, as specified by the {@code Accept-Charset} header.
     *
     * @param acceptableCharsets the acceptable charsets
     * @return this builder
     */
    default HttpRequestHeaders acceptCharset(Charset... acceptableCharsets) {
        return this.set(
                Headers.ACCEPT_CHARSET,
                Stream.of(acceptableCharsets)
                        .map(charset -> charset.name().toLowerCase(Locale.ENGLISH))
                        .collect(Collectors.joining(", ")));
    }


    /**
     * Performs the provided callback for each header.
     */
    void forEach(BiConsumer<String, List<String>> callback);
}
