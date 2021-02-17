package eu.xenit.contentcloud.opa.client.rest;

import eu.xenit.contentcloud.opa.client.api.io.http.ReactiveHttpClient.ReactiveHttpResponse;
import java.util.function.Consumer;

@FunctionalInterface
public interface ResponseListener extends Consumer<ReactiveHttpResponse> {

}
