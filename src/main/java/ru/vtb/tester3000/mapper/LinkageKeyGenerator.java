package ru.vtb.tester3000.mapper;

import java.security.SecureRandom;

/**
 * Generates shared linkage key for auth {@code match.key} and clearing {@code link.key}:
 * 40 uppercase hex characters + rrn.
 * Example: {@code C2C79672014882477C66D2192ABBB755DDB60FE7} + {@code 260802390625398999}.
 */
public final class LinkageKeyGenerator {

    private static final char[] HEX = "0123456789ABCDEF".toCharArray();
    private static final int PREFIX_LEN = 40;
    private static final int RRN_LEN = 18;

    private final SecureRandom random;

    public LinkageKeyGenerator() {
        this(new SecureRandom());
    }

    public LinkageKeyGenerator(SecureRandom random) {
        this.random = random;
    }

    public record LinkageKey(String key, String rrn) {
    }

    public LinkageKey generate() {
        String rrn = randomDigits(RRN_LEN);
        String prefix = randomHex(PREFIX_LEN);
        return new LinkageKey(prefix + rrn, rrn);
    }

    private String randomHex(int length) {
        char[] buf = new char[length];
        for (int i = 0; i < length; i++) {
            buf[i] = HEX[random.nextInt(HEX.length)];
        }
        return new String(buf);
    }

    private String randomDigits(int length) {
        char[] buf = new char[length];
        for (int i = 0; i < length; i++) {
            buf[i] = (char) ('0' + random.nextInt(10));
        }
        return new String(buf);
    }
}
