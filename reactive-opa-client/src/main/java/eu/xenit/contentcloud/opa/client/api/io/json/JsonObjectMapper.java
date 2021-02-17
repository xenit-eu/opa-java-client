package eu.xenit.contentcloud.opa.client.api.io.json;

import java.io.InputStream;

public interface JsonObjectMapper {

    <T> T deserialize(byte[] source, Class<T> type) throws DeserializationException;
    <T> byte[] serialize(T data) throws SerializationException;

}
