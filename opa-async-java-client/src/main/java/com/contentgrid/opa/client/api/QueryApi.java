package com.contentgrid.opa.client.api;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.Data;
import lombok.EqualsAndHashCode;

public interface QueryApi {

    <T> CompletableFuture<QueryResponse> query(String query);

    @Data
    class QueryResponse {
        private List<QueryResultEntry> result;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    class QueryResultEntry extends HashMap<String, Object> {

    }
}
