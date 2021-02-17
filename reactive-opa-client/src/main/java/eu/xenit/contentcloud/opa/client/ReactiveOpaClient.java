package eu.xenit.contentcloud.opa.client;

import eu.xenit.contentcloud.opa.client.api.CompileApi;
import eu.xenit.contentcloud.opa.client.api.DataApi;
import eu.xenit.contentcloud.opa.client.api.PolicyApi;
import eu.xenit.contentcloud.opa.client.api.io.http.JdkHttpClient;
import eu.xenit.contentcloud.opa.client.api.io.http.ReactiveHttpClient;
import eu.xenit.contentcloud.opa.client.api.io.json.JacksonObjectMapper;
import eu.xenit.contentcloud.opa.client.api.io.json.JsonObjectMapper;
import eu.xenit.contentcloud.opa.client.impl.DataComponent;
import eu.xenit.contentcloud.opa.client.impl.CompileComponent;
import eu.xenit.contentcloud.opa.client.impl.PolicyComponent;
import eu.xenit.contentcloud.opa.client.rest.RequestListener;
import eu.xenit.contentcloud.opa.client.rest.ResponseListener;
import eu.xenit.contentcloud.opa.client.rest.OpaRestClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

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
    public <TData> CompletableFuture<Void> upsertData(String path, TData data) {
        return this.dataApi.upsertData(path, data);
    }


    /**
     * Builder for {@link ReactiveOpaClient}
     */
    public static class Builder {

        private String url = "http://localhost:8181";
        private ReactiveHttpClient httpClient = JdkHttpClient.newClient();
        private JsonObjectMapper jsonMapper = new JacksonObjectMapper();

        private List<RequestListener> requestListeners = new ArrayList<>();
        private List<ResponseListener> reponselisteners = new ArrayList<>();

        /**
         * @param url URL including protocol and port
         */
        public Builder url(String url) {
            Objects.requireNonNull(httpClient);
            this.url = url;
            return this;
        }

        public Builder httpClient(ReactiveHttpClient httpClient) {
            Objects.requireNonNull(httpClient);
            this.httpClient = httpClient;
            return this;
        }

        public Builder jsonMapper(JsonObjectMapper jsonMapper) {
            Objects.requireNonNull(httpClient);
            this.jsonMapper = jsonMapper;
            return this;
        }

        public Builder onRequest(RequestListener listener) {
            Objects.requireNonNull(listener);
            this.requestListeners.add(listener);
            return this;
        }

        public Builder onResponse(ResponseListener listener) {
            Objects.requireNonNull(listener);
            this.reponselisteners.add(listener);
            return this;
        }

        public ReactiveOpaClient build() {
            var config = new OpaConfiguration(this.url);

            OpaRestClient opaRestClient = new OpaRestClient(config, httpClient, jsonMapper, this.requestListeners, this.reponselisteners);
            return new ReactiveOpaClient(
                    new PolicyComponent(opaRestClient),
                    new DataComponent(opaRestClient),
                    new CompileComponent(opaRestClient));

        }
    }
}
