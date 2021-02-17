package eu.xenit.contentcloud.opa.client.rest;

import eu.xenit.contentcloud.opa.client.api.io.http.ReactiveHttpClient.HttpRequestSpec;
import java.util.function.Consumer;

@FunctionalInterface
public interface RequestListener extends Consumer<HttpRequestSpec<?>> {

}
