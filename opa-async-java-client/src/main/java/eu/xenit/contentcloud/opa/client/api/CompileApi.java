package eu.xenit.contentcloud.opa.client.api;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import eu.xenit.contentcloud.opa.rego.ast.QuerySet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public interface CompileApi {

    CompletableFuture<PartialEvalResponse> compile(PartialEvaluationRequest request);

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
        QuerySet queries;

        @JsonAnySetter
        Map<String, Object> unknown;
    }





}
