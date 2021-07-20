package eu.xenit.contentcloud.thunx.opa.client.api;

import java.util.concurrent.CompletableFuture;
import lombok.Data;

public interface DataApi {

    <TData> CompletableFuture<UpsertDataResult> upsertData(String path, TData data);

    <TData> CompletableFuture<TData> getData(String path, Class<TData> responseType);

    @Data
    class GetDataResponse<TData> {
        TData result;
    }

    @Data
    class UpsertDataResult {
        // HTTP 304 - not modified
        // HTTP 204 - no content
    }

}
