package eu.xenit.contentcloud.opa.client.rest;

import static org.assertj.core.api.Assertions.assertThat;

import eu.xenit.contentcloud.opa.client.OpaConfiguration;
import eu.xenit.contentcloud.opa.client.http.JdkHttpClient;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import lombok.Data;
import org.junit.jupiter.api.Test;

class OpaRestClientTest {

    private OpaRestClient opaClient = new OpaRestClient(
            new OpaConfiguration("https://api.my-ip.io/"),
            JdkHttpClient.newClient());

    @Data
    static class MyIpDotComResult {
        boolean success;
        String ip;
        String type;
    }

    @Test
    void getMyIpDotIo_intoPOJO() throws ExecutionException, InterruptedException {
        var result = opaClient.get("ip.json", MyIpDotComResult.class).get();

        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getIp()).isNotEmpty();
        assertThat(result.getType()).isNotEmpty();
    }

    static class Result extends HashMap<String, Object>{}

    @Test
    void getMyIpDotIo_intoMapSubclass() throws ExecutionException, InterruptedException {
        Map<String, Object> result = opaClient.get("ip.json", Result.class).get();

        assertThat(result).isNotNull()
                .containsEntry("success", true)
                .containsKey("ip")
                .containsKey("type");

    }

}