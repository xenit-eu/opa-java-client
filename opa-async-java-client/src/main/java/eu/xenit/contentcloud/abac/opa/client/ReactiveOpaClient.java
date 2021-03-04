package eu.xenit.contentcloud.abac.opa.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.xenit.contentcloud.abac.opa.client.api.DataApi;
import eu.xenit.contentcloud.abac.opa.client.impl.PolicyComponent;
import eu.xenit.contentcloud.abac.opa.client.api.CompileApi;
import eu.xenit.contentcloud.abac.opa.client.api.PolicyApi;
import eu.xenit.contentcloud.abac.opa.client.impl.DataComponent;
import eu.xenit.contentcloud.abac.opa.client.impl.CompileComponent;
import eu.xenit.contentcloud.abac.opa.client.rest.OpaRestClient;
import eu.xenit.contentcloud.abac.opa.client.rest.RestClientConfiguration.LogSpecification;
import eu.xenit.contentcloud.abac.opa.client.rest.client.jdk.DefaultOpaRestClient;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

public class ReactiveOpaClient implements PolicyApi, DataApi, CompileApi {

    private final CompileApi compileApi;
    private final DataApi dataApi;
    private final PolicyApi policyApi;

    ReactiveOpaClient(PolicyApi policyApi, DataApi dataApi, CompileApi compileApi) {
        this.policyApi = policyApi;
        this.dataApi = dataApi;
        this.compileApi = compileApi;
    }

    /**
     * @return builder for {@link ReactiveOpaClient}
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
     * Builder for {@link ReactiveOpaClient}
     */
    @Slf4j
    public static class Builder {

        private String url = "http://localhost:8181";
        private OpaRestClient restClient = null;

        /**
         * @param url URL including protocol and port
         */
        public Builder url(String url) {
            Objects.requireNonNull(url);
            this.url = url;
            return this;
        }

        public Builder restClient(OpaRestClient restClient) {
            Objects.requireNonNull(restClient);
            this.restClient = restClient;
            return this;
        }

        public ReactiveOpaClient build() {

            var opaRestClient = this.getOrCreateDefaultRestClient();

            
            return new ReactiveOpaClient(
                    new PolicyComponent(opaRestClient),
                    new DataComponent(opaRestClient),
                    new CompileComponent(opaRestClient));

        }

        private OpaRestClient getOrCreateDefaultRestClient() {
            var client = this.restClient;

            // create the default client, if not provided by the builder
            if (client == null) {
                var httpClient = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(5))
                        .followRedirects(Redirect.NORMAL)
                        .build();
                client = new DefaultOpaRestClient(httpClient, new ObjectMapper());
            }

            // configure the provided client
            client.configure(config -> config
                    .baseUrl(this.url)
                    .logging(LogSpecification::all));

            return client;
        }
    }
}
