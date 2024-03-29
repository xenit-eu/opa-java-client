package com.contentgrid.opa.client;

import com.contentgrid.opa.client.api.CompileApi;
import com.contentgrid.opa.client.api.DataApi;
import com.contentgrid.opa.client.api.QueryApi;
import com.contentgrid.opa.client.impl.CompileComponent;
import com.contentgrid.opa.client.impl.DataComponent;
import com.contentgrid.opa.client.impl.PolicyComponent;
import com.contentgrid.opa.client.impl.QueryComponent;
import com.contentgrid.opa.client.rest.OpaHttpClient;
import com.contentgrid.opa.client.rest.RestClientConfiguration.LogSpecification;
import com.contentgrid.opa.client.rest.client.jdk.DefaultOpaHttpClient;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.contentgrid.opa.client.api.PolicyApi;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;

public class OpaClient implements PolicyApi, QueryApi, DataApi, CompileApi {

    private final CompileApi compileComponent;
    private final QueryApi queryComponent;
    private final DataApi dataComponent;
    private final PolicyApi policyComponent;

    OpaClient(PolicyApi policyComponent, QueryApi queryComponent, DataApi dataComponent, CompileApi compileComponent) {
        this.policyComponent = policyComponent;
        this.queryComponent = queryComponent;
        this.dataComponent = dataComponent;
        this.compileComponent = compileComponent;
    }

    /**
     * @return builder for {@link OpaClient}
     */
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public CompletableFuture<PartialEvalResponse> compile(PartialEvaluationRequest request) {
        return this.compileComponent.compile(request);
    }

    @Override
    public CompletableFuture<ListPoliciesResponse> listPolicies() {
        return this.policyComponent.listPolicies();
    }

    @Override
    public CompletableFuture<UpsertPolicyResponse> upsertPolicy(String id, String policy) {
        return this.policyComponent.upsertPolicy(id, policy);
    }

    @Override
    public CompletableFuture<GetPolicyResponse> getPolicy(String id) {
        return this.policyComponent.getPolicy(id);
    }

    @Override
    public CompletableFuture<DeletePolicyResponse> deletePolicy(String id) {
        return this.policyComponent.deletePolicy(id);
    }

    @Override
    public <TData> CompletableFuture<UpsertDataResult> upsertData(String path, TData data) {
        return this.dataComponent.upsertData(path, data);
    }

    @Override
    public <TData> CompletableFuture<TData> getData(String path, Class<TData> responseType) {
        return this.dataComponent.getData(path, responseType);
    }

    @Override
    public CompletableFuture<QueryResponse> query(String query) {
        return this.queryComponent.query(query);
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
                HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(5))
                        .followRedirects(Redirect.NORMAL)
                        .build(),
                JsonMapper.builder()
                        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                        .build()
                        .registerModule(new JavaTimeModule()));

        private Consumer<LogSpecification> httpLogSpec = LogSpecification::verbose;

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

        public Builder httpLogging(Consumer<LogSpecification> httpLogSpec) {
            Objects.requireNonNull(httpLogSpec);
            this.httpLogSpec = httpLogSpec;
            return this;
        }

        public OpaClient build() {

            var opaHttpClient = this.getOrCreateDefaultHttpClient();

            return new OpaClient(
                    new PolicyComponent(opaHttpClient),
                    new QueryComponent(opaHttpClient),
                    new DataComponent(opaHttpClient),
                    new CompileComponent(opaHttpClient));

        }

        protected OpaHttpClient getOrCreateDefaultHttpClient() {
            var client = this.restClient;

            // configure the provided client
            client.configure(config -> config
                    .baseUrl(this.url)
                    .logging(this.httpLogSpec));

            return client;
        }
    }
}
