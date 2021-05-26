package eu.contentcloud.opa.client.api;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.Data;

public interface QueryApi {

    <T> CompletableFuture<QueryResponse> query(String query);

    @Data
    class QueryResponse {
        private List<QueryResultEntry> result;
    }

    @Data
    class QueryResultEntry extends HashMap<String, Object> {

    }
}
