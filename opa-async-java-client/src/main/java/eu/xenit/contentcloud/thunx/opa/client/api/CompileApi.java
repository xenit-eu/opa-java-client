package eu.xenit.contentcloud.thunx.opa.client.api;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import eu.xenit.contentcloud.thunx.opa.rego.ast.QuerySet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public interface CompileApi {

    CompletableFuture<PartialEvalResponse> compile(PartialEvaluationRequest request);

    default CompletableFuture<PartialEvalResponse> compile(String query, Object input, List<String> unknowns) {
        return this.compile(new PartialEvaluationRequest(query, input, unknowns));
    }

    default CompletableFuture<PartialEvalResponse> compile(String query, Object input) {
        return this.compile(new PartialEvaluationRequest(query, input, null));
    }

    @Getter
    @RequiredArgsConstructor
    class PartialEvaluationRequest {

        final String query;
        final Object input;
        final List<String> unknowns;

    }

    @Data
    class PartialEvalResponse {
        PartialEvalResult result;
    }

    @Data
    class PartialEvalResult {

        /**
         * When partially evaluate a query with the Compile API, OPA returns a new set of queries/conditions.
         *
         * If ONE of those conditions can be satisfied, the query is true.
         * If a query is always true, this QuerySet will contain an empty array.
         * If this field is null, that indicates there are NO conditions that could make the query true.
         */
        QuerySet queries;

        @JsonAnySetter
        Map<String, Object> unknown;
    }





}
