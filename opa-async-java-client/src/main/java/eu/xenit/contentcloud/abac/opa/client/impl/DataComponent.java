package eu.xenit.contentcloud.abac.opa.client.impl;

import eu.xenit.contentcloud.abac.opa.client.api.DataApi;
import eu.xenit.contentcloud.abac.opa.client.rest.OpaHttpClient;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DataComponent implements DataApi {

    private static final String DATA_ENDPOINT = "/v1/data/";

    private final OpaHttpClient opaHttpClient;

    @Override
    public <TData> CompletableFuture<Void> upsertData(String path, TData data) {
        return opaHttpClient.put(DATA_ENDPOINT + path, data);
    }

    @Override
    public <TData> CompletableFuture<TData> getData(String path, Class<TData> responseType) {
        return opaHttpClient.get(DATA_ENDPOINT + path, responseType);
    }


}
