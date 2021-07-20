package eu.xenit.contentcloud.opa.client.rest.client.jdk.converter;

import eu.xenit.contentcloud.opa.client.rest.http.MediaType;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class StringConverter implements HttpBodyConverter {

    private static final List<MediaType> MEDIA_TYPES = List.of(MediaType.TEXT_PLAIN, new MediaType("text", "*"));;

    private final Charset defaultCharset;

    /**
     * Default constructor that uses {@code "ISO-8859-1"} as default character set.
     *
     * This is specified as the default for text/* by RFC2616
     *
     * @see <a href="https://tools.ietf.org/html/rfc2616#section-3.7.1">Section 3.7.1 of [RFC2616]</a>
     */
    public StringConverter() {
        this(StandardCharsets.ISO_8859_1);
    }

    /**
     * A constructor accepting a default charset to use if the requested content type does not specify one.
     */
    public StringConverter(Charset defaultCharset) {
        this.defaultCharset = Objects.requireNonNull(defaultCharset, "defaultCharset cannot be null");
    }

    @Override
    public <T> boolean canRead(DeserializationContext context, Class<T> type) {
        return type == String.class && this.supportsMediaType(context.getContentType());
    }

    @Override
    public <T> T read(DeserializationContext context, Class<T> type) {
        var charset = context.getContentType() == null
                ? this.defaultCharset
                : context.getContentType().getCharset().orElse(this.defaultCharset);

        return type.cast(new String(context.getSource(), charset));

    }

    @Override
    public boolean canWrite(SerializationContext context) {
        return context.getSource().getClass() == String.class && this.supportsMediaType(context.getContentType());
    }

    @Override
    public byte[] write(SerializationContext context) {
        var source = context.getSource();
        if (source == null) {
            return new byte[0];
        }

        var charset = context.getContentType() == null
                ? this.defaultCharset
                : context.getContentType().getCharset().orElse(this.defaultCharset);

        return source.toString().getBytes(charset);
    }

    @Override
    public Collection<MediaType> getSupportedMediaTypes() {
        return MEDIA_TYPES;
    }
}
