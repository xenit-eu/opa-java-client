package eu.xenit.contentcloud.opa.client.api;

import eu.xenit.contentcloud.opa.rego.ast.AbstractSyntaxTree;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.Data;

public interface PolicyApi {

    CompletableFuture<ListPoliciesResponse> listPolicies();

    CompletableFuture<UpsertPolicyResponse> upsertPolicy(String id, String policy);

    CompletableFuture<GetPolicyResponse> getPolicy(String id);

    CompletableFuture<DeletePolicyResponse> deletePolicy(String id);

    @Data
    class ListPoliciesResponse {
        private List<OpaPolicy> result;
    }

    @Data
    class UpsertPolicyResponse {

    }

    @Data
    class GetPolicyResponse {
        private OpaPolicy result;
    }

    @Data
    class DeletePolicyResponse {

    }

    @Data
    class OpaPolicy {

        private String id;
        private String raw;
        private AbstractSyntaxTree ast;

    }
}
