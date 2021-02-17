package eu.xenit.contentcloud.opa.client.api;

import eu.xenit.contentcloud.opa.client.api.model.OpaPolicy;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.Data;

public interface PolicyApi {

    CompletableFuture<ListPoliciesResponse> listPolicies();

    @Data
    class ListPoliciesResponse {
        List<OpaPolicy> result;
    }
}
