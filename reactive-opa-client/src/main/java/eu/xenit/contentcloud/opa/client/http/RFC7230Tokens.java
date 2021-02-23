package eu.xenit.contentcloud.opa.client.http;

import java.util.BitSet;

public final class RFC7230Tokens {

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

    public static boolean isValidToken(String token) {
        return token.chars().allMatch(TOKEN::get);
    }

    public static void assertIsValidToken(String token) {
        token.chars().forEach(ch -> {
            if (!TOKEN.get(ch)) {
                throw new IllegalArgumentException("Invalid token character '" + ch + "' in token \"" + token + "\"");
            }
        });
    }

}
