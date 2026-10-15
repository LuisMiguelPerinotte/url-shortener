package com.luismiguel.java.url_shortener.domain.entity;

import com.luismiguel.java.url_shortener.domain.exception.shorturl.InvalidURLException;
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
    @DisplayName("normalize")
    class Normalize {

        @DisplayName("Should Return Null When Url Is Null")
        @Test
        void shouldReturnNullWhenUrlIsNull() {
            assertNull(ShortURL.normalize(null));
        }

        @DisplayName("Should Trim Surrounding Spaces From Url")
        @Test
        void shouldTrimSurroundingSpacesFromUrl() {
            assertEquals("https://x.com", ShortURL.normalize("  https://x.com  "));
        }

        @DisplayName("Should Return Url Unchanged When It Has No Surrounding Spaces")
        @Test
        void shouldReturnUrlUnchangedWhenItHasNoSurroundingSpaces() {
            assertEquals("https://x.com", ShortURL.normalize("https://x.com"));
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

        @DisplayName("Should Throw IllegalStateException When Already Set")
        @Test
        void shouldThrowIllegalStateExceptionWhenAlreadySet() {
            assertThrows(IllegalStateException.class, () -> {
                shortURL.assignShortCode("newShortCode");
            });
        }
    }
}
