package com.contentgrid.opa.client.rest.client.jdk.converter;

import com.contentgrid.opa.client.rest.http.MediaType;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

public class JacksonBodyConverter implements HttpBodyConverter {

    private static final List<MediaType> MEDIA_TYPES = List.of(MediaType.APPLICATION_JSON, new MediaType("application", "*+json"));

    private final JsonMapper jsonMapper;

    public JacksonBodyConverter() {
        this(JsonMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
                .build());
    }

    public JacksonBodyConverter(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    @Override
    public <T> boolean canRead(DeserializationContext context, Class<T> type) {
        Objects.requireNonNull(context, "Argument 'context' cannot be null");
        Objects.requireNonNull(type, "Argument 'type' cannot be null");

        // support is determined by media type only
        return this.supportsMediaType(context.getContentType());
    }

    @Override
    public Collection<MediaType> getSupportedMediaTypes() {
        return MEDIA_TYPES;
    }

    @Override
    public <T> T read(DeserializationContext context, Class<T> type) {
        Objects.requireNonNull(context, "Argument 'context' cannot be null");
        Objects.requireNonNull(type, "Argument 'type' cannot be null");

        return jsonMapper.readValue(context.getSource(), type);
    }

    @Override
    public boolean canWrite(SerializationContext context) {
        Objects.requireNonNull(context, "Argument 'context' cannot be null");

        // support is determined by media type only
        return this.supportsMediaType(context.getContentType());
    }

    @Override
    public byte[] write(SerializationContext context) {
        Objects.requireNonNull(context, "Argument 'context' cannot be null");

        return jsonMapper.writeValueAsBytes(context.getSource());
    }
}
