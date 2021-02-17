package eu.xenit.contentcloud.opa.client.rest;

import static org.assertj.core.api.Assertions.assertThat;

import eu.xenit.contentcloud.opa.client.OpaConfiguration;
import eu.xenit.contentcloud.opa.client.api.io.http.JdkHttpClient;
import eu.xenit.contentcloud.opa.client.api.io.json.JacksonObjectMapper;
import java.util.concurrent.ExecutionException;
import lombok.Data;
import org.junit.jupiter.api.Test;

class OpaRestClientTest {

    @Data
    static class MyIpDotComResult {
        String ip;
        String country;
        String cc;
    }

    @Test
    void getMyIpDotCom() throws ExecutionException, InterruptedException {
        var opaClient = new OpaRestClient(
                new OpaConfiguration("https://api.myip.com/"),
                JdkHttpClient.newClient(),
                new JacksonObjectMapper());

        var result = opaClient.get("/", MyIpDotComResult.class).get();

        assertThat(result).isNotNull();
        assertThat(result.getIp()).isNotEmpty();
        assertThat(result.getCountry()).isNotEmpty();
        assertThat(result.getCc()).isNotEmpty();
    }

}