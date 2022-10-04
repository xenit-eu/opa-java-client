package com.contentgrid.opa.client.impl;

import com.contentgrid.opa.client.rest.OpaHttpClient;
import com.contentgrid.opa.client.api.PolicyApi;
import com.contentgrid.opa.client.rest.http.MediaType;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PolicyComponent implements PolicyApi {

    private static final String POLICY_ENDPOINT = "/v1/policies/";

    private final OpaHttpClient restClient;

    @Override
    public CompletableFuture<ListPoliciesResponse> listPolicies() {
        return this.restClient.get(POLICY_ENDPOINT, ListPoliciesResponse.class);
    }

    @Override
    public CompletableFuture<UpsertPolicyResponse> upsertPolicy(String id, String policy) {
        return this.restClient.put(POLICY_ENDPOINT + id,
                headers -> headers.contentType(MediaType.TEXT_PLAIN),
                policy, UpsertPolicyResponse.class);

    }

    @Override
    public CompletableFuture<GetPolicyResponse> getPolicy(String id) {
        return this.restClient.get(POLICY_ENDPOINT + id, GetPolicyResponse.class);
    }

    @Override
    public CompletableFuture<DeletePolicyResponse> deletePolicy(String id) {
        return this.restClient.delete(POLICY_ENDPOINT + id, DeletePolicyResponse.class);
    }
}
