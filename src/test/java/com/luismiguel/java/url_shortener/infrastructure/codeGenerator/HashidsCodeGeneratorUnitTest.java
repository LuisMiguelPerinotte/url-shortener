package com.luismiguel.java.url_shortener.infrastructure.codeGenerator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class HashidsCodeGeneratorUnitTest {
    private HashidsCodeGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new HashidsCodeGenerator("test-salt", 6);
    }

    @DisplayName("Should Generate Same Code For Same Id")
    @Test
    void shouldGenerateSameCodeForSameId() {
        assertEquals(generator.generate(100L), generator.generate(100L));
    }

    @DisplayName("Should Generate Unique Codes For Many Ids")
    @Test
    void shouldGenerateUniqueCodesForManyIds() {
        Set<String> codes = new HashSet<>();

        for (long i = 0; i < 1000; i++) {
            codes.add(generator.generate(i));
        }

        assertEquals(1000, codes.size());
    }

    @DisplayName("Should Respect Minimum Length")
    @Test
    void shouldRespectMinimumLength() {
        List<Long> ids = List.of(1L, 100L, 999999L, 9000000000000L);

        for (Long id : ids) {
            assertTrue(generator.generate(id).length() >= 6);
        }
    }

    @DisplayName("Should Generate Different Codes For Different Ids")
    @Test
    void shouldGenerateDifferentCodesForDifferentIds() {
        assertNotEquals(generator.generate(552L), generator.generate(65372L));
    }
}
