package eu.xenit.contentcloud.opa.client.api.io.http;

import java.net.URI;
import java.util.List;
import java.util.Optional;

public interface HttpHeaders {

    final class HeaderNames {
        private HeaderNames() { }

        public static final String LOCATION = "Location";
        public static final String CONTENT_TYPE = "Content-Type";
    }


    /**
     * Get the list of header values for the given header name, if any.
     * @param headerName the header name
     * @return the list of header values, or an empty list
     */
    List<String> get(String headerName);


    /**
     * Return the first header value for the given header name, if any.
     * @param headerName the header name
     * @return the first header value, or {@code Optional.empty()} if none
     */
    default Optional<String> getFirst(String headerName) {
        return this.get(headerName).stream().findFirst();
    }

    /**
     * Return the first header value for the given header name, if any.
     * @param headerName the header name
     * @return the first header value, or {@code null} if none
     */
    default Optional<String> getFirstOrEmpty(String headerName) {
        return this.get(headerName).stream().findFirst();
    }


    /**
     * Return the (new) location of a resource as specified by the {@code Location} header.
     * <p>Returns {@code Optional.empty()} when the location is unknown.
     */
    default Optional<URI> getLocation() {
        return this.getFirst(HeaderNames.LOCATION).map(URI::create);
    }

    /**
     * Return the content-type of the body, as specified by the {@code Content-Type} header.
     *
     * @return The {@link ContentType} if present, otherwise {@code Optional.empty()}
     */
    default Optional<ContentType> getContentType() {
        return getFirst(HeaderNames.CONTENT_TYPE).map(contentType -> new ContentType(){
            @Override
            public String toString() {
                return contentType;
            }
        });
    }
}
