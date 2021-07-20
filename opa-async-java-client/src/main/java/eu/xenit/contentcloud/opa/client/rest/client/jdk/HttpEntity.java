package eu.xenit.contentcloud.opa.client.rest.client.jdk;

import eu.xenit.contentcloud.opa.client.rest.http.HttpRequestHeaders;
import java.util.Optional;

public class HttpEntity {

    private final HttpRequestHeaders headers;
    private final byte[] body;

    public HttpEntity(HttpRequestHeaders headers, byte[] body) {
        this.headers = headers;
        this.body = body;
    }

    public HttpEntity(HttpRequestHeaders headers) {
        this(headers, null);
    }

    public HttpRequestHeaders getHeaders() {
        return headers;
    }

    public Optional<byte[]> getBody() {
        return Optional.ofNullable(body);
    }
}
