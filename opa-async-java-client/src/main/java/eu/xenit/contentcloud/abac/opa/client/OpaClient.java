package eu.xenit.contentcloud.abac.opa.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.xenit.contentcloud.abac.opa.client.api.DataApi;
import eu.xenit.contentcloud.abac.opa.client.impl.PolicyComponent;
import eu.xenit.contentcloud.abac.opa.client.api.CompileApi;
import eu.xenit.contentcloud.abac.opa.client.api.PolicyApi;
import eu.xenit.contentcloud.abac.opa.client.impl.DataComponent;
import eu.xenit.contentcloud.abac.opa.client.impl.CompileComponent;
import eu.xenit.contentcloud.abac.opa.client.rest.OpaHttpClient;
import eu.xenit.contentcloud.abac.opa.client.rest.RestClientConfiguration.LogSpecification;
import eu.xenit.contentcloud.abac.opa.client.rest.client.jdk.DefaultOpaHttpClient;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

public class OpaClient implements PolicyApi, DataApi, CompileApi {

    private final CompileApi compileApi;
    private final DataApi dataApi;
    private final PolicyApi policyApi;

    OpaClient(PolicyApi policyApi, DataApi dataApi, CompileApi compileApi) {
        this.policyApi = policyApi;
        this.dataApi = dataApi;
        this.compileApi = compileApi;
    }

    /**
     * @return builder for {@link OpaClient}
     */
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public CompletableFuture<PartialEvalResponse> compile(PartialEvaluationRequest request) {
        return this.compileApi.compile(request);
    }

    @Override
    public CompletableFuture<ListPoliciesResponse> listPolicies() {
         return this.policyApi.listPolicies();
    }

    @Override
    public CompletableFuture<UpsertPolicyResponse> upsertPolicy(String id, String policy) {
        return this.policyApi.upsertPolicy(id, policy);
    }

    @Override
    public CompletableFuture<GetPolicyResponse> getPolicy(String id) {
        return this.policyApi.getPolicy(id);
    }

    @Override
    public CompletableFuture<DeletePolicyResponse> deletePolicy(String id) {
        return this.policyApi.deletePolicy(id);
    }

    @Override
    public <TData> CompletableFuture<Void> upsertData(String path, TData data) {
        return this.dataApi.upsertData(path, data);
    }

    @Override
    public <TData> CompletableFuture<TData> getData(String path, Class<TData> responseType) {
        return this.dataApi.getData(path, responseType);
    }


    /**
     * Builder for {@link OpaClient}
     */
    @Slf4j
    public static class Builder {

        /**
         * The default url
         */
        private String url = "http://localhost:8181";

        /**
         * The default rest-client
         */
        private OpaHttpClient restClient = new DefaultOpaHttpClient(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).followRedirects(Redirect.NORMAL).build(),
                new ObjectMapper());

        /**
         * @param url URL including protocol and port
         */
        public Builder url(String url) {
            Objects.requireNonNull(url);
            this.url = url;
            return this;
        }

        public Builder restClient(OpaHttpClient restClient) {
            Objects.requireNonNull(restClient);
            this.restClient = restClient;
            return this;
        }

        public OpaClient build() {

            var opaRestClient = this.getOrCreateDefaultRestClient();

            return new OpaClient(
                    new PolicyComponent(opaRestClient),
                    new DataComponent(opaRestClient),
                    new CompileComponent(opaRestClient));

        }

        private OpaHttpClient getOrCreateDefaultRestClient() {
            var client = this.restClient;

            // configure the provided client
            client.configure(config -> config
                    .baseUrl(this.url)
                    .logging(LogSpecification::verbose));

            return client;
        }
    }
}
