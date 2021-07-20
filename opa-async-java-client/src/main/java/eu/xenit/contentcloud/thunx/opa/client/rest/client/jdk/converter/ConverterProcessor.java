package eu.xenit.contentcloud.thunx.opa.client.rest.client.jdk.converter;

import eu.xenit.contentcloud.thunx.opa.client.rest.client.jdk.HttpEntity;
import eu.xenit.contentcloud.thunx.opa.client.rest.client.jdk.converter.HttpBodyConverter.DeserializationContext;
import eu.xenit.contentcloud.thunx.opa.client.rest.client.jdk.converter.HttpBodyConverter.SerializationContext;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;

public class ConverterProcessor {

    private final Collection<? extends HttpBodyConverter> objectMappers;

    public ConverterProcessor(Collection<? extends HttpBodyConverter> objectMappers) {
        this.objectMappers = Collections.unmodifiableSet(new LinkedHashSet<>(objectMappers));
    }

    public <T> T read(DeserializationContext context, Class<T> type) {

        if (Void.TYPE.equals(type) || Void.class.equals(type)) {
            return null;
        }

        return this.objectMappers.stream()

                // figure out which object-mapper CAN read the Content-Type
                .filter(mapper -> mapper.canRead(context, type))
                .findFirst()

                // if none found, bail out with an exception
                .orElseThrow(() -> {
                    String msg = String.format("Cannot convert %s into %s", context.getContentType().toString(), type.getName());
                    return new UncheckedIOException(new IOException(msg));
                })

                // if found, use it to deserialize the source into the target type
                .read(context, type);
    }

    public HttpEntity write(SerializationContext context) {

        if (context.isEmpty()) {
            return new HttpEntity(context.getHeaders());
        }

        byte[] buffer = this.objectMappers.stream()

                // figure out which object-mapper CAN write to this content type
                .filter(mapper -> mapper.canWrite(context))
                .findFirst()

                // if none found, bail out with an exception
                .orElseThrow(() -> {
                    String msg = String.format("Cannot convert %s into %s",
                            context.getSource().getClass().getName(), context.getContentType());
                    return new UncheckedIOException(new IOException(msg));
                })

                // if found, use it to deserialize the source into the target type
                .write(context);

        return new HttpEntity(context.getHeaders(), buffer);
    }

}
