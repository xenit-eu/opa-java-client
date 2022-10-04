package com.contentgrid.opa.client.impl;

import com.contentgrid.opa.client.api.QueryApi;
import com.contentgrid.opa.client.rest.OpaHttpClient;
import java.util.concurrent.CompletableFuture;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class QueryComponent implements QueryApi {

    private static final String QUERY_ENDPOINT = "/v1/query";

    private final OpaHttpClient httpClient;

    @Override
    public CompletableFuture<QueryResponse> query(String query) {
        return this.query(new QueryRequest(query));
    }

    CompletableFuture<QueryResponse> query(QueryRequest request) {
        return this.httpClient.post(QUERY_ENDPOINT, request, QueryResponse.class);
    }

    @Data
    static class AdHocQueryResponse<T> {
        T result;
    }

    @Data
    @AllArgsConstructor
    private class QueryRequest {
        @NonNull
        String query;
    }
}
