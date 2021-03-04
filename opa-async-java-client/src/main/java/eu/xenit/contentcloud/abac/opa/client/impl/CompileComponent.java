package eu.xenit.contentcloud.abac.opa.client.impl;

import eu.xenit.contentcloud.abac.opa.client.api.CompileApi;
import eu.xenit.contentcloud.abac.opa.client.rest.OpaRestClient;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CompileComponent implements CompileApi {

    private static final String COMPILE_ENDPOINT = "/v1/compile";

    private final OpaRestClient restClient;

    @Override
    public CompletableFuture<PartialEvalResponse> compile(PartialEvaluationRequest request) {
        return restClient.post(COMPILE_ENDPOINT, request, PartialEvalResponse.class);
    }
}
