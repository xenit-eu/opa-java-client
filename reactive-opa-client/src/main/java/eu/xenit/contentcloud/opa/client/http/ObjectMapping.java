package eu.xenit.contentcloud.opa.client.http;

import eu.xenit.contentcloud.opa.client.http.mapper.DeserializationException;
import eu.xenit.contentcloud.opa.client.http.mapper.ObjectMapper;
import eu.xenit.contentcloud.opa.client.http.mapper.ObjectMapper.DeserializationContext;
import eu.xenit.contentcloud.opa.client.http.mapper.ObjectMapper.SerializationContext;
import eu.xenit.contentcloud.opa.client.http.mapper.SerializationException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;

class ObjectMapping {

    private final Collection<? extends ObjectMapper> objectMappers;

    public ObjectMapping(Collection<? extends ObjectMapper> objectMappers) {
        this.objectMappers = Collections.unmodifiableSet(new LinkedHashSet<>(objectMappers));
    }

    public <T> T read(DeserializationContext context, Class<T> type) {

        return this.objectMappers.stream()

                // figure out which object-mapper CAN read the Content-Type
                .filter(mapper -> mapper.canRead(context, type))
                .findFirst()

                // if none found, bail out with an exception
                .orElseThrow(() -> {
                    String msg = String.format("Cannot convert %s into %s", context.getContentType().toString(), type.getName());
                    return new DeserializationException(msg);
                })

                // if found, use it to deserialize the source into the target type
                .read(context, type);
    }

    public byte[] write(SerializationContext context) {
        return this.objectMappers.stream()

                // figure out which object-mapper CAN write to this content type
                .filter(mapper -> mapper.canWrite(context))
                .findFirst()

                // if none found, bail out with an exception
                .orElseThrow(() -> {
                    String msg = String.format("Cannot convert %s into %s",
                            context.getSource().getClass().getName(), context.getContentType());
                    return new SerializationException(msg);
                })

                // if found, use it to deserialize the source into the target type
                .write(context);
    }

}
