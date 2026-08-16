package ru.vtb.tester3000.mapper;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LinkageKeyGeneratorTest {

    @Test
    void generatesFortyHexPlusRrn() {
        LinkageKeyGenerator.LinkageKey key = new LinkageKeyGenerator().generate();
        assertEquals(18, key.rrn().length());
        assertTrue(key.rrn().chars().allMatch(Character::isDigit));
        assertEquals(58, key.key().length());
        assertTrue(key.key().startsWith(key.key().substring(0, 40)));
        assertTrue(key.key().endsWith(key.rrn()));
        assertTrue(key.key().substring(0, 40).chars().allMatch(c ->
                (c >= '0' && c <= '9') || (c >= 'A' && c <= 'F')));
    }
}
