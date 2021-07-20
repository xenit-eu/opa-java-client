package eu.xenit.contentcloud.thunx.opa.client.rest.client.jdk;

import eu.xenit.contentcloud.thunx.opa.client.rest.http.HttpRequestHeaders;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;

public class HttpHeadersBuilder implements HttpRequestHeaders {

    private final HashMap<String, List<String>> headersMap = new HashMap<>();

    @Override
    public HttpRequestHeaders add(String headerName, String headerValue) {
        this.headersMap.computeIfAbsent(headerName, (k) -> new ArrayList<>(1)).add(headerValue);
        return this;
    }

    @Override
    public HttpRequestHeaders set(String headerName, String... headerValues) {
        List<String> values = new ArrayList<>(Arrays.asList(headerValues));
        this.headersMap.put(headerName, values);
        return this;
    }

    @Override
    public HttpRequestHeaders remove(String headerName) {
        this.headersMap.remove(headerName);
        return this;
    }

    @Override
    public List<String> get(String headerName) {
        List<String> values = this.headersMap.getOrDefault(headerName, Collections.emptyList());
        return Collections.unmodifiableList(values);
    }

    public void forEach(BiConsumer<String, List<String>> callback) {
        this.headersMap.forEach(callback);
    }
}
