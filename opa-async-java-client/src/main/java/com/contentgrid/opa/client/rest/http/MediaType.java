package com.contentgrid.opa.client.rest.http;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Represents a RFC 2046 Media Type.
 *
 * A media type consists of {@code type/subtype} and MAY be followed by parameters in the form of
 * {@code}name=value{code} paris.
 *
 * @see <a href="https://tools.ietf.org/html/rfc2046">RFC 2046 - Media Types</a>
 * @see <a href="https://tools.ietf.org/html/rfc7231#section-3.1.1.1">HTTP 1.1: Semantics and Content, section
 * 3.1.1.1</a>
 */
public class MediaType {

    public static final MediaType APPLICATION_JSON = new MediaType("application", "json");
    public static final MediaType APPLICATION_OCTET_STREAM = new MediaType("application", "octet-stream");

    public static final MediaType TEXT_ALL = new MediaType("text", "*");
    public static final MediaType TEXT_PLAIN = new MediaType("text", "plain");

    protected static final String WILDCARD = "*";

    private static final String PARAM_CHARSET = "charset";
    private static final String PARAM_Q = "q";

    private final String type;
    private final String subtype;
    private final Map<String, String> parameters;

    /**
     * The resolved character set, possibly {@code null}.
     */
    private Charset charset;


    /**
     * Create a new {@code MediaType} for the given primary type and subtype, with empty parameters.
     *
     * @param type the primary type
     * @param subtype the subtype
     * @throws IllegalArgumentException if any of the arguments contain illegal characters
     */
    public MediaType(String type, String subtype) {
        this(type, subtype, Collections.emptyMap());
    }

    /**
     * Create a new {@code MediaType} for the given type, subtype, and parameters.
     *
     * @param type the primary type
     * @param subtype the subtype
     * @param parameters the parameters, may be {@code null}
     * @throws IllegalArgumentException if any of the arguments contain illegal characters
     */
    public MediaType(String type, String subtype, Map<String, String> parameters) {
        assertNotEmpty(type, "'type' must not be empty");
        assertNotEmpty(subtype, "'subtype' must not be empty");

        this.type = type.toLowerCase(Locale.ENGLISH);
        this.subtype = subtype.toLowerCase(Locale.ENGLISH);

        if (parameters.isEmpty()) {
            this.parameters = Collections.emptyMap();
        } else {
            this.parameters = new LinkedHashMap<>();
            parameters.forEach((key, value) -> {
                this.checkParameters(key, value);
                this.parameters.put(key, value);
            });
        }
    }


    /**
     * Return the primary type.
     */
    public String getType() {
        return this.type;
    }

    /**
     * Return the subtype.
     */
    public String getSubtype() {
        return this.subtype;
    }

    /**
     * Return the character set, as indicated by a {@code charset} parameter, if available.
     *
     * @return the character set wrapped in an {@code Optional}, or {@code Optional.empty()} if not available
     */
    public Optional<Charset> getCharset() {
        return Optional.ofNullable(this.charset);
    }

    /**
     * Indicates whether the {@linkplain #getSubtype() subtype} is the wildcard character <code>&#42;</code> or the
     * wildcard character followed by a suffix (e.g. <code>&#42;+xml</code>).
     *
     * @return whether the subtype is a wildcard
     */
    public boolean isWildcardSubtype() {
        return WILDCARD.equals(getSubtype()) || getSubtype().startsWith("*+");
    }


    /**
     * Return a generic parameter value, given a parameter name.
     *
     * @param name the parameter name
     * @return the parameter value, or {@code null} if not present
     */
    public String getParameter(String name) {
        return this.parameters.get(name);
    }

    /**
     * Returns all media type parameters in a read-only Map
     *
     * @return an immutable map with all parameters, never {@code null}
     */
    public Map<String, String> getParameters() {
        return Collections.unmodifiableMap(this.parameters);
    }
    /**
     * Return the quality factor, as indicated by a {@code q} parameter, if any. Defaults to {@code 1.0}.
     *
     * @return the quality factor as double value
     */
    public double getQualityValue() {
        String qualityFactor = getParameter(PARAM_Q);
        return (qualityFactor != null ? Double.parseDouble(unquote(qualityFactor)) : 1D);
    }

    /**
     * Indicate whether this {@code MediaType} includes the given media type.
     *
     * For example, {@code text/*} should includes {@code text/plain} and {@code text/html}, and {@code
     * application/*+xml} includes {@code application/soap+xml}.
     *
     * @param other the reference media type with which to compare
     * @return {@code true} if this media type includes the given media type; {@code false} otherwise
     */
    public boolean includes(MediaType other) {
        if (other == null) {
            return false;
        }

        // */* includes anything
        if ("*".equals(this.getType())) {
            return true;
        }

        // check that the types match
        else if (this.getType().equals(other.getType())) {

            if (this.getSubtype().equals(other.getSubtype())) {
                // the subtypes match!
                return true;
            } else if (WILDCARD.equals(this.getSubtype())) {
                // our subtype is the wildcard
                return true;
            } else if (this.getSubtype().startsWith("*+")) {
                // Wildcard with suffix, e.g. application/*+xml
                // application/*+xml includes application/soap+xml
                int thisPlusIdx = getSubtype().indexOf('+');
                int otherPlusIdx = other.getSubtype().indexOf('+');
                if (otherPlusIdx != -1) {
                    String thisSubtypeSuffix = getSubtype().substring(thisPlusIdx + 1);
                    String otherSubtypeSuffix = other.getSubtype().substring(otherPlusIdx + 1);

                    // check that the suffix (non-wildcard) part matches
                    return thisSubtypeSuffix.equals(otherSubtypeSuffix);
                }
            }
        }
        return false;
    }

    public static MediaType parseMediaType(String mediaType) {
        int index = mediaType.indexOf(';');
        String fullType = (index >= 0 ? mediaType.substring(0, index) : mediaType).trim();
        if (fullType.isEmpty()) {
            throw new IllegalArgumentException("Invalid media type '" + mediaType + "': 'mediaType' must not be empty");
        }

        // re-write a plain '*' into '*/*'
        // java.net.HttpURLConnection returns a *; q=.2 Accept header
        if (WILDCARD.equals(fullType)) {
            fullType = "*/*";
        }

        // make sure there is an '/' somewhere in the fullType
        int subIndex = fullType.indexOf('/');
        if (subIndex == -1) {
            throw new IllegalArgumentException("Invalid media type '" + mediaType + "': does not contain '/'");
        }

        // the '/' should not be in the last position
        if (subIndex == fullType.length() - 1) {
            throw new IllegalArgumentException(
                    "Invalid media type '" + mediaType + "': does not contain subtype after '/'");
        }

        String type = fullType.substring(0, subIndex);
        String subtype = fullType.substring(subIndex + 1);

        // if the type is a wildcard, the subtype can't be specific
        if (WILDCARD.equals(type) && !WILDCARD.equals(subtype)) {
            throw new IllegalArgumentException(
                    "Invalid media type '" + mediaType + "': wildcard type is legal only in '*/*' (all mime types)");
        }

        Map<String, String> parameters = new LinkedHashMap<>(4);
        do {
            int nextIndex = index + 1;
            boolean quoted = false;

            // this splits on ';' - but only if the string is not quoted
            while (nextIndex < mediaType.length()) {
                char ch = mediaType.charAt(nextIndex);
                if (ch == ';') {
                    if (!quoted) {
                        break;
                    }
                } else if (ch == '"') {
                    quoted = !quoted;
                }
                nextIndex++;
            }

            // substring out the parameter
            String parameter = mediaType.substring(index + 1, nextIndex).trim();
            if (parameter.length() > 0) {
                int eqIndex = parameter.indexOf('=');
                if (eqIndex >= 0) {
                    String attribute = parameter.substring(0, eqIndex).trim();
                    String value = parameter.substring(eqIndex + 1).trim();
                    parameters.put(attribute, value);
                }
            }
            index = nextIndex;
        }
        while (index < mediaType.length());

        return new MediaType(type, subtype, parameters);
    }

    public static List<MediaType> parseMediaTypes(String... mediaTypes) {
        return Arrays.stream(mediaTypes)
                .map(MediaType::parseMediaType)
                .collect(Collectors.toList());
    }


    @Override
    public String toString() {
        var sb = new StringBuilder()
                .append(this.type)
                .append('/')
                .append(this.subtype);

        this.parameters.forEach((key, val) -> {
            sb
                    .append(";")
                    .append(key)
                    .append('=')
                    .append(val);

        });

        return sb.toString();
    }

    /**
     * Return a string representation of the given list of {@code MediaType} objects, to be used in an {@code Accept} or
     * {@code Content-Type} header.
     *
     * @param mediaTypes the media types to create a string representation for
     * @return the string representation
     */
    public static String toString(Collection<MediaType> mediaTypes) {
        return mediaTypes.stream().map(Object::toString).collect(Collectors.joining(", "));
    }

    private void checkParameters(String parameter, String value) {
        assertNotEmpty(parameter, "'parameter' must not be empty");
        assertNotEmpty(value, "'value' must not be empty");
        RFC7230Tokens.assertIsValidToken(parameter);

        if (PARAM_Q.equals(parameter.toLowerCase(Locale.ENGLISH))) {
            value = unquote(value);
            double d = Double.parseDouble(value);
            if (d < 0D || d > 1D) {
                throw new IllegalArgumentException(
                        "Invalid quality value \"" + value + "\": should be between 0.0 and 1.0");
            }
        } else if (PARAM_CHARSET.equals(parameter.toLowerCase(Locale.ENGLISH))) {
            if (this.charset == null) {
                this.charset = Charset.forName(unquote(value));
            }
        }
    }

    private static void assertNotEmpty(String test, String message) {
        if (test == null || test.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }


    private static String unquote(String string) {
        return isDoubleQuoted(string) ? string.substring(1, string.length() - 1) : string;
    }

    private static boolean isDoubleQuoted(String s) {
        return s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"");
    }

    private static final class RFC7230Tokens {

        private RFC7230Tokens() { }

        /**
         * token          = 1*tchar
         * tchar          = "!" / "#" / "$" / "%" / "&" / "'" / "*"
         *                / "+" / "-" / "." / "^" / "_" / "`" / "|" / "~"
         *                / DIGIT / ALPHA
         *                ; any VCHAR, except delimiters
         *
         * @see <a href="https://tools.ietf.org/html/rfc7230#section-3.2.6">RFC 7230, section 3.2.6.  Field Value Components</a>
         */
        static final BitSet TOKEN;

        static {
            TOKEN = new BitSet(128);
            TOKEN.set('!');
            TOKEN.set('#');
            TOKEN.set('$');
            TOKEN.set('%');
            TOKEN.set('&');
            TOKEN.set('\'');
            TOKEN.set('*');
            TOKEN.set('+');
            TOKEN.set('-');
            TOKEN.set('.');
            TOKEN.set('^');
            TOKEN.set('_');
            TOKEN.set('`');
            TOKEN.set('|');
            TOKEN.set('~');
            TOKEN.set('0', '9' + 1); // DIGIT
            TOKEN.set('a', 'z' + 1); // alpha
            TOKEN.set('A', 'Z' + 1); // ALPHA

        }

        public static void assertIsValidToken(String token) {
            token.chars().forEach(ch -> {
                if (!TOKEN.get(ch)) {
                    throw new IllegalArgumentException("Invalid token character '" + ch + "' in token \"" + token + "\"");
                }
            });
        }

    }
}
