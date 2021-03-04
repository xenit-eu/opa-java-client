package eu.xenit.contentcloud.abac.opa.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import eu.xenit.contentcloud.abac.opa.client.api.CompileApi;
import eu.xenit.contentcloud.abac.opa.client.api.CompileApi.PartialEvaluationRequest;
import eu.xenit.contentcloud.abac.opa.client.api.DataApi;
import eu.xenit.contentcloud.abac.opa.client.api.DataApi.GetDocumentResponse;
import eu.xenit.contentcloud.abac.opa.client.api.PolicyApi.ListPoliciesResponse;
import eu.xenit.contentcloud.abac.opa.client.rest.http.HttpStatusException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
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

    private final OpaClient opaClient = OpaClient.builder()
            .url("http://" + opaContainer.getHost() + ":" + opaContainer.getMappedPort(OPA_EXPOSED_PORT))
            .build();

    @Test
    void opaIsRunningInDocker() {
        assertThat(opaContainer.isRunning()).isTrue();
    }

    @Nested
    class PolicyTests {

        private static final String PATH_EXAMPLE_1 = "fixtures/openpolicyagent.org/examples/policies-example-1.txt";
        private static final String PATH_EXAMPLE_2 = "fixtures/openpolicyagent.org/examples/policies-example-2.txt";

        @Test
        @Order(1)
        void upsertPlainTextPolicy_shouldSucceed() throws IOException {
            String content = loadResourceAsString(PATH_EXAMPLE_1);

            var result = opaClient.upsertPolicy("example1", content).join();
            Assertions.assertThat(result).isNotNull();

            var getPolicyResponse = opaClient.getPolicy("example1").join();
            Assertions.assertThat(getPolicyResponse.getResult()).isNotNull();
            Assertions.assertThat(getPolicyResponse.getResult().getRaw()).isEqualTo(content);
        }

        @Test
        void upsertBogusPolicy_shouldFail() {
            String content = "invalid policy";

            var result = opaClient.upsertPolicy("example1", content);

            assertThatExceptionOfType(CompletionException.class).isThrownBy(result::join)
                    .havingCause().isInstanceOfSatisfying(HttpStatusException.class, httpStatusEx -> {
                assertThat(httpStatusEx.statusCode()).isEqualTo(400);
            });
        }

        @Test
        @Order(2)
        void deletePolicy_shouldSucceed() throws IOException {
            // first add another policy
            // Note: PATH_EXAMPLE_2 seems to fail with HTTP 400 message: "var public_servers is unsafe"
            // - is this a problem with the examples ?
            opaClient.upsertPolicy("example2", loadResourceAsString(PATH_EXAMPLE_1)).join();

            // fetch it first
            var upsertResponse = opaClient.getPolicy("example2").join();
            Assertions.assertThat(upsertResponse.getResult()).isNotNull();

            // now delete it again
            var deleteResponse = opaClient.deletePolicy("example2").join();
            Assertions.assertThat(deleteResponse).isNotNull();

            // check that a GET gives an error
            assertThatExceptionOfType(CompletionException.class)
                    .isThrownBy(() -> opaClient.getPolicy("example2").join())
                    .havingCause()
                    .isInstanceOfSatisfying(HttpStatusException.class, httpStatusEx -> {
                        assertThat(httpStatusEx.statusCode()).isEqualTo(404);
                    });
        }

        @Test
        @Order(3)
        void listPolicy() throws ExecutionException, InterruptedException {
            ListPoliciesResponse response = opaClient.listPolicies().get();

            assertThat(response).isNotNull();
            assertThat(response.getResult()).hasSize(1);
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
            // load data.pi into OPA
            opaClient.upsertData("/", Map.of("pi", 3.14D)).get();

            // fetch it again
            var data = opaClient.getData("/", GetDocumentResponse.class).join();
            assertThat(data.getResult())
                    .isNotNull()
                    .hasFieldOrPropertyWithValue("pi", 3.14D);

            // Use OPA's Compile API to partially evaluate a query. Treat 'input.radius' as unknown.
            var compile = opaClient.compile(new PartialEvaluationRequest(
                    "(data.pi * input.radius * 2) >= input.min_radius",
                    Map.of("min_radius", 4),
                    List.of("input.radius"))).get();

            Assertions.assertThat(compile).isNotNull();
            Assertions.assertThat(compile.getResult()).isNotNull();
            Assertions.assertThat(compile.getResult().getQueries())
                    .isNotNull()
                    .singleElement() // 1 query
                    .satisfies(query -> Assertions.assertThat(query)
                            .singleElement() // 1 expression
                            .satisfies(expr -> Assertions.assertThat(expr.getTerms()).satisfies(terms -> {
                                Assertions.assertThat(terms).hasSize(3);
                                // 1: gte
                                // 2: call(mul, call(mul, 3.14, input.radius), 2)
                                // 3: 4
                            })));
        }

    }

    private static String loadResourceAsString(String resourcePath) {
        try {
            return new String(
                    Objects.requireNonNull(ClassLoader.getSystemResourceAsStream(resourcePath)).readAllBytes(),
                    StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}