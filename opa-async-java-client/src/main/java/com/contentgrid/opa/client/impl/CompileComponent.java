package com.contentgrid.opa.client.impl;

import com.contentgrid.opa.client.api.CompileApi;
import com.contentgrid.opa.client.rest.OpaHttpClient;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CompileComponent implements CompileApi {

    private static final String COMPILE_ENDPOINT = "/v1/compile";

    private final OpaHttpClient restClient;

    @Override
    public CompletableFuture<PartialEvalResponse> compile(PartialEvaluationRequest request) {
        return restClient.post(COMPILE_ENDPOINT, request, PartialEvalResponse.class);
    }
}
