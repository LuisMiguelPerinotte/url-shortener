package com.luismiguel.java.url_shortener.domain.entity;

import com.luismiguel.java.url_shortener.infrastructure.exception.business.shortURL.InvalidURLException;
import com.luismiguel.java.url_shortener.infrastructure.exception.business.shortURL.ShortCodeAlreadyAssignedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ShortURLUnitTest {
    @Nested
    @DisplayName("create")
    class Create {
        ShortURL shortURL;

        @DisplayName("Should Create Short URL With Trimmed Url And Active True")
        @Test
        void shouldCreateShortURLWithTrimmedUrlAndActiveTrue() {
            shortURL = ShortURL.create("  originalURL  ");
            assertTrue(shortURL.getActive());
            assertEquals("originalURL", shortURL.getOriginalUrl());
        }

        @DisplayName("Should Throw InvalidUrlException When Url Is Null")
        @Test
        void shouldThrowInvalidUrlExceptionWhenUrlIsNull() {
            assertThrows(InvalidURLException.class, () -> {
                shortURL = ShortURL.create(null);
            });
        }

        @DisplayName("Should Throw InvalidUrlException When Url Is Blank")
        @Test
        void shouldThrowInvalidUrlExceptionWhenUrlIsBlank() {
            assertThrows(InvalidURLException.class, () -> {
                shortURL = ShortURL.create("");
            });
        }
    }

    @Nested
    @DisplayName("assignShortCode")
    class AssignShortCode {
        ShortURL shortURL;

        @BeforeEach
        void setUp() {
            shortURL = ShortURL.create("originalURL");
            shortURL.assignShortCode("shortCode");
        }

        @DisplayName("Should Assign Short Code When Not Already Set")
        @Test
        void shouldAssignShortCodeWhenNotAlreadySet() {
            assertEquals("shortCode", shortURL.getShortCode());
        }

        @DisplayName("Should Throw ShortCodeAlreadyAssignedException When Already Set")
        @Test
        void shouldThrowShortCodeAlreadyAssignedExceptionWhenAlreadySet() {
            assertThrows(ShortCodeAlreadyAssignedException.class, () -> {
                shortURL.assignShortCode("newShortCode");
            });
        }
    }
}
