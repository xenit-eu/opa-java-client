package eu.xenit.contentcloud.opa.client.impl;

import eu.xenit.contentcloud.opa.client.api.DataApi;
import eu.xenit.contentcloud.opa.client.rest.OpaRestClient;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DataComponent implements DataApi {

    private static final String DATA_ENDPOINT = "/v1/data/";

    private final OpaRestClient opaRestClient;

    @Override
    public <TData> CompletableFuture<Void> upsertData(String path, TData data) {
        return opaRestClient.put(DATA_ENDPOINT + path, data);
    }

    @Override
    public <TData> CompletableFuture<TData> getData(String path, Class<TData> responseType) {
        return opaRestClient.get(DATA_ENDPOINT + path, responseType);
    }
}
