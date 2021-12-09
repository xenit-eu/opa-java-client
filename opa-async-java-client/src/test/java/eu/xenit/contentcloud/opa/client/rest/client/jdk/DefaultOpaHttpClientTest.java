package eu.xenit.contentcloud.opa.client.rest.client.jdk;

import eu.xenit.contentcloud.opa.client.OpaClient;
import eu.xenit.contentcloud.opa.client.rest.client.jdk.converter.HttpBodyConverter;
import eu.xenit.contentcloud.opa.client.rest.http.MediaType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

class DefaultOpaHttpClientTest {

    @Test
    void defaultOpaClient_shouldSupportJdk8TimeApi() {
        var opaClient = new OpaClient.Builder() {
            DefaultOpaHttpClient getHttpClient() {
                return (DefaultOpaHttpClient) super.getOrCreateDefaultHttpClient();
            }
        }.getHttpClient();

        opaClient.converterProcessor.write(HttpBodyConverter.SerializationContext.of(
                Map.of("auth_time", Instant.now()),
                new HttpHeadersBuilder().add("Content-Type", MediaType.APPLICATION_JSON.toString())
        ));
    }

}