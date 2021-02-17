package eu.xenit.contentcloud.opa.client.impl;

import eu.xenit.contentcloud.opa.client.api.PolicyApi;
import eu.xenit.contentcloud.opa.client.rest.OpaRestClient;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PolicyComponent implements PolicyApi {

    private static final String COMPILE_ENDPOINT = "/v1/policies";

    private final OpaRestClient restClient;

    @Override
    public CompletableFuture<ListPoliciesResponse> listPolicies() {
        return this.restClient.get(COMPILE_ENDPOINT, ListPoliciesResponse.class);
    }
}
