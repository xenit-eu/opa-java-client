package eu.xenit.contentcloud.opa.client.api;

import java.util.List;
import java.util.concurrent.CompletableFuture;
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

    class PartialEvalResponse {

    }
}
