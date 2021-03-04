package eu.xenit.contentcloud.abac.opa.client.impl;

import eu.xenit.contentcloud.abac.opa.client.api.PolicyApi;
import eu.xenit.contentcloud.abac.opa.client.rest.OpaHttpClient;
import eu.xenit.contentcloud.abac.opa.client.rest.http.MediaType;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PolicyComponent implements PolicyApi {

    private static final String POLICY_ENDPOINT = "/v1/policies";

    private final OpaHttpClient restClient;

    @Override
    public CompletableFuture<ListPoliciesResponse> listPolicies() {
        return this.restClient.get(POLICY_ENDPOINT, ListPoliciesResponse.class);
    }

    @Override
    public CompletableFuture<UpsertPolicyResponse> upsertPolicy(String id, String policy) {
        return this.restClient.put(POLICY_ENDPOINT + "/" + id,
                headers -> headers.contentType(MediaType.TEXT_PLAIN),
                policy, UpsertPolicyResponse.class);

    }

    @Override
    public CompletableFuture<GetPolicyResponse> getPolicy(String id) {
        return this.restClient.get(POLICY_ENDPOINT + "/" + id, GetPolicyResponse.class);
    }

    @Override
    public CompletableFuture<DeletePolicyResponse> deletePolicy(String id) {
        return this.restClient.delete(POLICY_ENDPOINT + "/" + id, DeletePolicyResponse.class);
    }
}
