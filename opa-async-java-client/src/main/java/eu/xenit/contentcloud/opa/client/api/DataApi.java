package eu.xenit.contentcloud.opa.client.api;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.Data;

public interface DataApi {

    <TData> CompletableFuture<Void> upsertData(String path, TData data);

    <TData> CompletableFuture<TData> getData(String path, Class<TData> responseType);

    @Data
    class GetDocumentResponse<TData> {
        TData result;
    }
}
