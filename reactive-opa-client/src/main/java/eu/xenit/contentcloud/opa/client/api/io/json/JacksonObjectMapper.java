package eu.xenit.contentcloud.opa.client.api.io.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.io.IOException;
import java.util.Objects;

public class JacksonObjectMapper implements JsonObjectMapper {

    private final ObjectMapper objectMapper;

    public JacksonObjectMapper() {
        this(JsonMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build());
    }

    public JacksonObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public <T> T deserialize(byte[] source, Class<T> type) throws DeserializationException {
        Objects.requireNonNull(source, "Argument 'stream' cannot be null");
        Objects.requireNonNull(type, "Argument 'type' cannot be null");

        try {
            return objectMapper.readValue(source, type);
        } catch (IOException ex) {
            throw new DeserializationException(ex);
        }
    }

    @Override
    public <T> byte[] serialize(T data) throws SerializationException {
        Objects.requireNonNull(data, "Argument 'data' cannot be null");
        try {
            return objectMapper.writeValueAsBytes(data);
        } catch (JsonProcessingException e) {
            throw new SerializationException(e);
        }

    }
}
