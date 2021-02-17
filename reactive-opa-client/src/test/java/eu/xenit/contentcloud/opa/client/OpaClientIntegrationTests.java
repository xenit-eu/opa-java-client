package eu.xenit.contentcloud.opa.client;

import static org.assertj.core.api.Assertions.assertThat;

import eu.xenit.contentcloud.opa.client.api.CompileApi;
import eu.xenit.contentcloud.opa.client.api.CompileApi.PartialEvaluationRequest;
import eu.xenit.contentcloud.opa.client.api.DataApi;
import eu.xenit.contentcloud.opa.client.api.PolicyApi.ListPoliciesResponse;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Slf4j
@Testcontainers
class OpaClientIntegrationTests {

    private static final DockerImageName OPA_IMAGE = DockerImageName.parse("openpolicyagent/opa");
    private static final int OPA_EXPOSED_PORT = 8181;

    @Container
    private static final GenericContainer<?> opaContainer = new GenericContainer<>(OPA_IMAGE)
            .withCommand(String.format("run --server --addr :%d", OPA_EXPOSED_PORT))
            .withExposedPorts(OPA_EXPOSED_PORT)
            .waitingFor(Wait.forHttp("/"));

    private final ReactiveOpaClient opaClient = ReactiveOpaClient.builder()
            .url("http://" + opaContainer.getHost() + ":" + opaContainer.getMappedPort(OPA_EXPOSED_PORT))
            .onRequest(request -> request.log(log::info))
            .onResponse(response -> response.log(log::info))
            .build();

    @Test
    void opaIsRunningInDocker() {
        assertThat(opaContainer.isRunning()).isTrue();
    }

//    private static OpaRestClient opaRestClient() {
//        var url = "http://" + opaContainer.getHost() + ":" + opaContainer.getMappedPort(OPA_EXPOSED_PORT);
//        var config = new OpaConfiguration(url);
//        return new OpaRestClient(config, JdkHttpClient.newClient(), new JacksonObjectMapper());
//    }


    @Nested
    class PolicyTests {

        @Test
        void listPolicy() throws ExecutionException, InterruptedException {
            ListPoliciesResponse response = opaClient.listPolicies().get();

            assertThat(response).isNotNull();
            assertThat(response.getResult()).isEmpty();
        }
    }

    @Nested
    class Scenarios {

        /**
         * Simple scenario from the from the <a href="https://github.com/open-policy-agent/rego-python#example-load-rego-ast-from-json">rego-python</a>
         * project README, that makes use of the {@link DataApi} and the {@link CompileApi}.
         *
         * <ul>
         *     <li>Load some pi into OPA as data.</li>
         *     <li>Use OPA's Compile API to partially evaluate a query. Treat 'input.radius' as unknown.</li>
         *     <li>Load the resulting set of query ASTs out of the JSON response.</li>
         * </ul>
         */
        @Test
        void regoPythonSample() throws ExecutionException, InterruptedException {
            @Getter
            class PiDocument {
                double pi = Math.PI;
            }

            // load PiDocument into OPA as data.
            opaClient.upsertData("/", new PiDocument()).get();

            // Use OPA's Compile API to partially evaluate a query. Treat 'input.radius' as unknown.
            var result = opaClient.compile(new PartialEvaluationRequest(
                    "(data.pi * input.radius * 2) >= input.min_radius",
                    Map.of("min_radius", 4),
                    List.of("input.radius"))).get();

            log.info("test test");


        }

    }

}