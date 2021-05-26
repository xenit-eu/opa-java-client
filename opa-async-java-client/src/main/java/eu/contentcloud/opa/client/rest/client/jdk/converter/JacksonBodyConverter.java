package eu.contentcloud.opa.client.rest.client.jdk.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import eu.contentcloud.opa.client.rest.http.MediaType;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class JacksonBodyConverter implements HttpBodyConverter {

    private static final List<MediaType> MEDIA_TYPES = List.of(MediaType.APPLICATION_JSON, new MediaType("application", "*+json"));

    private final ObjectMapper objectMapper;

    public JacksonBodyConverter() {
        this(JsonMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build());
    }

    public JacksonBodyConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public <T> boolean canRead(DeserializationContext context, Class<T> type) {
        Objects.requireNonNull(context, "Argument 'context' cannot be null");
        Objects.requireNonNull(type, "Argument 'type' cannot be null");

        // check the media type
        if (!this.supportsMediaType(context.getContentType())) {
            return false;
        }

        var javaType = objectMapper.constructType(type);

        AtomicReference<Throwable> causeRef = new AtomicReference<>();
        if (this.objectMapper.canDeserialize(javaType, causeRef)) {
            return true;
        }

        // TODO we could log causeRef
        return false;

    }

    @Override
    public Collection<MediaType> getSupportedMediaTypes() {
        return MEDIA_TYPES;
    }

    @Override
    public <T> T read(DeserializationContext context, Class<T> type) {
        Objects.requireNonNull(context, "Argument 'context' cannot be null");
        Objects.requireNonNull(type, "Argument 'type' cannot be null");

        try {
            return objectMapper.readValue(context.getSource(), type);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public boolean canWrite(SerializationContext context) {
        Objects.requireNonNull(context, "Argument 'context' cannot be null");

        // check if *any* of the content-types match
        if (!this.supportsMediaType(context.getContentType())) {
            return false;
        }


        AtomicReference<Throwable> causeRef = new AtomicReference<>();
        if (this.objectMapper.canSerialize(context.getSource().getClass())) {
            return true;
        }
        // TODO we could log causeRef

        return false;
    }

    @Override
    public byte[] write(SerializationContext context) {
        Objects.requireNonNull(context, "Argument 'context' cannot be null");

        try {
            return objectMapper.writeValueAsBytes(context.getSource());
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }

    }
}
