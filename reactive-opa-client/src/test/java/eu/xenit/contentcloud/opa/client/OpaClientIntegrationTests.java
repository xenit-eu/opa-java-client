package eu.xenit.contentcloud.opa.client;

import static org.assertj.core.api.Assertions.assertThat;

import eu.xenit.contentcloud.opa.client.api.CompileApi;
import eu.xenit.contentcloud.opa.client.api.CompileApi.PartialEvaluationRequest;
import eu.xenit.contentcloud.opa.client.api.DataApi;
import eu.xenit.contentcloud.opa.client.api.DataApi.GetDocumentResponse;
import eu.xenit.contentcloud.opa.client.api.PolicyApi.ListPoliciesResponse;
import eu.xenit.contentcloud.opa.client.api.model.Term.Call;
import eu.xenit.contentcloud.opa.client.api.model.Term.NumberValue;
import eu.xenit.contentcloud.opa.client.api.model.Term.Ref;
import eu.xenit.contentcloud.opa.client.api.model.Term.Var;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
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
            .build();

    @Test
    void opaIsRunningInDocker() {
        assertThat(opaContainer.isRunning()).isTrue();
    }

    @Nested
    class PolicyTests {

        @Test
        void listPolicy() throws ExecutionException, InterruptedException {
            ListPoliciesResponse response = opaClient.listPolicies().get();

            assertThat(response).isNotNull();
            assertThat(response.getResult()).isEmpty();
        }
    }

    public static class DataObject extends HashMap<String, Object> {

    }

    ;

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
            var data = opaClient.getData("/", GetDocumentResponse.class).get();
            assertThat(data.getResult())
                    .isNotNull()
                    .hasFieldOrPropertyWithValue("pi", 3.14D);

            // Use OPA's Compile API to partially evaluate a query. Treat 'input.radius' as unknown.
            var compile = opaClient.compile(new PartialEvaluationRequest(
                    "(data.pi * input.radius * 2) >= input.min_radius",
                    Map.of("min_radius", 4),
                    List.of("input.radius"))).get();

            assertThat(compile).isNotNull();
            assertThat(compile.getResult()).isNotNull();
            assertThat(compile.getResult().getQueries())
                    .isNotNull()
                    .singleElement() // 1 query
                    .satisfies(query -> assertThat(query)
                            .singleElement() // 1 expression
                            .satisfies(expr -> assertThat(expr.getTerms()).satisfies(terms -> {
                                assertThat(terms).first()
                                        .isInstanceOf(Ref.class)
                                        .hasFieldOrPropertyWithValue("value", List.of(new Var("gte")));
                                assertThat(terms).element(1)
                                        .isInstanceOfSatisfying(Call.class, call -> {
                                            assertThat(call.getValue()).first()
                                                    .isInstanceOf(Ref.class)
                                                    .hasFieldOrPropertyWithValue("value", List.of(new Var("mul")));
                                            // call
                                            //  ref var mul
                                            //  number
                                            //  ref
                                            //      var(input)
                                            //      string(radius)

                                        });

                                assertThat(terms).last()
                                        .isInstanceOf(NumberValue.class)
                                        .hasFieldOrPropertyWithValue("value", 4L);

                            })));
        }

    }

}