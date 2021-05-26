package eu.contentcloud.opa.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.InstanceOfAssertFactories.MAP;

import eu.contentcloud.abac.opa.rego.ast.AbstractSyntaxTree;
import eu.contentcloud.opa.client.api.CompileApi;
import eu.contentcloud.opa.client.api.CompileApi.PartialEvaluationRequest;
import eu.contentcloud.opa.client.api.DataApi;
import eu.contentcloud.opa.client.api.DataApi.GetDataResponse;
import eu.contentcloud.opa.client.api.PolicyApi;
import eu.contentcloud.opa.client.api.PolicyApi.ListPoliciesResponse;
import eu.contentcloud.opa.client.api.PolicyApi.UpsertPolicyResponse;
import eu.contentcloud.opa.client.rest.http.HttpStatusException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
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

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void opaIsRunningInDocker() {
        assertThat(opaContainer.isRunning()).isTrue();
    }

    /**
     * Examples from the REST Data API documentation
     *
     * @see <a href="https://www.openpolicyagent.org/docs/latest/rest-api/#policy-api">REST Policy API</a>
     */
    @Nested
    class PolicyApiTests {

        private static final String PATH_EXAMPLE_1 = "fixtures/openpolicyagent.org/docs/policy/policies-example-1.txt";
        private static final String PATH_EXAMPLE_2 = "fixtures/openpolicyagent.org/docs/policy/policies-example-2.txt";

        @Test
        @Order(1)
        void upsertPlainTextPolicy_shouldSucceed() throws IOException {
            String content = loadResourceAsString(PATH_EXAMPLE_1);

            var result = opaClient.upsertPolicy("example1", content).join();
            assertThat(result).isNotNull();

            var getPolicyResponse = opaClient.getPolicy("example1").join();
            assertThat(getPolicyResponse.getResult()).isNotNull();
            assertThat(getPolicyResponse.getResult().getRaw()).isEqualTo(content);
        }

        @Test
        void upsertBogusPolicy_shouldFail() {
            var result = opaClient.upsertPolicy("example1", "invalid policy");

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
            assertThat(upsertResponse.getResult()).isNotNull();

            // now delete it again
            var deleteResponse = opaClient.deletePolicy("example2").join();
            assertThat(deleteResponse).isNotNull();

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
            assertThat(response.getResult().size()).isGreaterThan(0);
        }
    }

    /**
     * Examples from the REST Data API documentation
     *
     * @see <a href="https://www.openpolicyagent.org/docs/latest/rest-api/#data-api">REST Data API</a>
     */
    @Nested
    class DataApiTests {

        private static final String PATH_DATA = "fixtures/openpolicyagent.org/docs/data/data.json";

        @Test
        void testCreateData_servers() throws IOException {
            Map<?, ?> data = objectMapper.readValue(loadResourceAsString(PATH_DATA), Map.class);
            opaClient.upsertData("/servers", data).join();

            var response = opaClient.getData("servers", GetServersResponse.class).join();
            assertThat(response.get("result"))
                    .isNotNull()
                    .asInstanceOf(MAP)
                    .containsKeys("networks", "servers", "ports");
        }

    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    static class GetServersResponse extends HashMap<String, Object> {

    }

    /**
     * Examples from the REST Query API documentation
     *
     * @see <a href="https://www.openpolicyagent.org/docs/latest/rest-api/#query-api">REST Query API</a>
     */
    @Nested
    class QueryApiTests {

        @Test
        void adhocQuery() throws IOException {
            // first upload some data
            Map<?, ?> data = objectMapper.readValue(loadResourceAsString(DataApiTests.PATH_DATA), Map.class);
            opaClient.upsertData("query", data).join();

            // Note: the docs use `.name`, but the example data set uses `.id`
            var query = "data.query.servers[i].ports[_] = \"p2\"; data.query.servers[i].id = id";
            var response = opaClient.query(query).join();

            assertThat(response).isNotNull();
            assertThat(response.getResult())
                    .hasSize(2)
                    .satisfiesExactly(
                            item -> assertThat(item).asInstanceOf(MAP).containsEntry("id", "app").containsEntry("i", 0),
                            item -> assertThat(item).asInstanceOf(MAP).containsEntry("id", "ci").containsEntry("i", 3)
                    );
        }
    }

    /**
     * Examples from the REST Compile API documentation
     *
     * @see <a href="https://www.openpolicyagent.org/docs/latest/rest-api/#compile-api">REST Compile API</a>
     */
    @Nested
    class CompileApiTests {

        public static final String POLICY_PATH = "fixtures/openpolicyagent.org/docs/compile/compile-api-policy.txt";

        @Test
        void unknownInputX_largerThan0() {
            var result = opaClient.compile(
                    "input.x > 0",
                    Map.of(),
                    List.of("input")).join();
            assertThat(result).isNotNull();
            assertThat(result.getResult()).isNotNull();
            assertThat(result.getResult().getQueries()).singleElement().satisfies(query -> {
                assertThat(query).singleElement().satisfies(expr -> {
                    assertThat(expr.getIndex()).isEqualTo(0);
                    assertThat(expr.getTerms()).hasSize(3);

                    assertThat(expr.getTerms().get(0).toString()).isEqualTo("gt");
                    assertThat(expr.getTerms().get(1).toString()).isEqualTo("input[\"x\"]");
                    assertThat(expr.getTerms().get(2).toString()).isEqualTo("0");
                });
            });
        }

        @Test
        void defaultInputUnknownX_largerThan0() {
            var result = opaClient.compile(
                    "input.x > 0",
                    Map.of()).join();
            assertThat(result).isNotNull();
            assertThat(result.getResult()).isNotNull();
            assertThat(result.getResult().getQueries()).singleElement().satisfies(query -> {
                assertThat(query).singleElement().satisfies(expr -> {
                    assertThat(expr.getIndex()).isEqualTo(0);
                    assertThat(expr.getTerms()).hasSize(3);

                    assertThat(expr.getTerms().get(0).toString()).isEqualTo("gt");
                    assertThat(expr.getTerms().get(1).toString()).isEqualTo("input[\"x\"]");
                    assertThat(expr.getTerms().get(2).toString()).isEqualTo("0");
                });
            });
        }

        @Test
        void alwaysTrue() {
            var result = opaClient.compile("1 > 0", null).join();

            assertThat(result).isNotNull();
            assertThat(result.getResult()).isNotNull();
            assertThat(result.getResult().getQueries())
                    .hasSize(1)
                    .singleElement()
                    .satisfies(query -> assertThat(query).isEmpty());
        }

        @Test
        void alwaysFalse() {
            var result = opaClient.compile("1 < 0", null).join();

            assertThat(result).isNotNull();
            assertThat(result.getResult()).isNotNull();
            assertThat(result.getResult().getQueries()).isNull();
        }


        @Test
        void compileExample() {
            opaClient.upsertPolicy("compile-example", loadResourceAsString(POLICY_PATH)).join();

            var request = new PartialEvaluationRequest(
                    "data.compile.example.allow == true",
                    Map.of("subject", Map.of("clearance_level", 4)),
                    List.of("data.reports"));

            var result = opaClient.compile(request).join();
            assertThat(result).isNotNull();
            assertThat(result.getResult()).isNotNull();
            assertThat(result.getResult().getQueries())
                    .isNotNull()
                    .singleElement()
                    .satisfies(query -> Assertions.assertThat(query)
                            .singleElement()
                            .satisfies(expr -> Assertions.assertThat(expr.getTerms()).satisfies(terms -> {
                                // gte ( 4, data.reports[_].clearance_level)
                                Assertions.assertThat(terms).hasSize(3);
                                // 1: ref:var:gte
                                // 2: number:4
                                // 3: ref:data.reports[_].clearance_level
                            })));
        }

        @Test
        void example_alwaysTrue() {
            opaClient.upsertPolicy("compile-example", loadResourceAsString(POLICY_PATH)).join();

            var request = new PartialEvaluationRequest(
                    "data.compile.example.allow == true",
                    Map.of("subject", Map.of("clearance_level", 4),
                            "break_glass", true),
                    List.of("data.reports"));

            var result = opaClient.compile(request).join();
            assertThat(result).isNotNull();
            assertThat(result.getResult()).isNotNull();
            assertThat(result.getResult().getQueries())
                    .isNotNull()
                    .hasSize(2)

                    // assert one (1) empty query, because the query can be satisfied without any further evaluation
                    .anySatisfy(query -> assertThat(query).isEmpty());
        }
    }

    @Nested
    class Scenarios {

        /**
         * Scenario for partial evaluation of a policy, based on 'group' attribute on a set of documents/results
         *
         * Context: 1. A user is member of a set of groups, expressed via an array as `input.user.groups[]` 2. Documents
         * have a (single) `group` attribute.
         *
         * Policy: A user can access a document, when the document has a `group` attribute that matches with a group
         * from his users' profile.
         */
        @Test
        void partialEval_getApiDocuments() {
            final String POLICY_PATH = "fixtures/scenarios/api-documents-policy.txt";
            opaClient.upsertPolicy("test", loadResourceAsString(POLICY_PATH)).join();

            var result = opaClient.compile(
                    "data.test.allow == true",
                    Map.of("method", "GET",
                            "path", List.of("api", "documents"),
                            "user", Map.of("group", List.of("my-group"))
                    ),
                    List.of("data.documents")
            ).join();
            assertThat(result).isNotNull();
            assertThat(result.getResult()).isNotNull();
            assertThat(result.getResult().getQueries())
                    .singleElement()
                    .satisfies(query -> Assertions.assertThat(query)
                            .singleElement()
                            .satisfies(expr -> {
                                assertThat(expr.getIndex()).isEqualTo(0);
                                var terms = expr.getTerms();
                                assertThat(terms).hasSize(3);
                                assertThat(terms.get(0).toString()).isEqualTo("eq");
                                assertThat(terms.get(1).toString()).isEqualTo("\"my-group\"");
                                assertThat(terms.get(2).toString())
                                        .isEqualTo("data[\"documents\"][$11][\"group\"]");
                            }));
        }

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
            var data = opaClient.getData("/", GetDataResponse.class).join();
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
        try (var stream = ClassLoader.getSystemResourceAsStream(resourcePath)) {
            Objects.requireNonNull(stream, "Classpath resource not found: " + resourcePath);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}