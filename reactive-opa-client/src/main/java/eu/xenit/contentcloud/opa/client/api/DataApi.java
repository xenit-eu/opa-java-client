package eu.xenit.contentcloud.opa.client.api;

import java.util.concurrent.CompletableFuture;

public interface DataApi {

    <TData> CompletableFuture<Void> upsertData(String path, TData data);

    class WriteDataResponse {

    }
}
