package com.luismiguel.java.url_shortener.web.dto.shorturl.request;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShortenURLRequestDTOTest {
    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private int violationsFor(String url) {
        return validator.validate(new ShortenURLRequestDTO(url)).size();
    }

    @Nested
    @DisplayName("Valid URLs")
    class ValidUrls {

        @DisplayName("Should Accept Https URL")
        @Test
        void shouldAcceptHttpsUrl() {
            assertEquals(0, violationsFor("https://example.com/some-page"));
        }

        @DisplayName("Should Accept Http URL")
        @Test
        void shouldAcceptHttpUrl() {
            assertEquals(0, violationsFor("http://example.com/some-page"));
        }

        @DisplayName("Should Accept URL With Exactly 2048 Characters")
        @Test
        void shouldAcceptUrlWithExactlyMaxLength() {
            String url = buildUrlWithLength(2048);
            assertEquals(2048, url.length());
            assertEquals(0, violationsFor(url));
        }
    }

    @Nested
    @DisplayName("Invalid URLs")
    class InvalidUrls {

        @DisplayName("Should Reject Ftp URL")
        @Test
        void shouldRejectFtpUrl() {
            assertTrue(violationsFor("ftp://example.com") > 0);
        }

        @DisplayName("Should Reject File URL")
        @Test
        void shouldRejectFileUrl() {
            assertTrue(violationsFor("file:///etc/passwd") > 0);
        }

        @DisplayName("Should Reject Javascript URL")
        @Test
        void shouldRejectJavascriptUrl() {
            assertTrue(violationsFor("javascript:alert(1)") > 0);
        }

        @DisplayName("Should Reject URL Without Scheme")
        @Test
        void shouldRejectUrlWithoutScheme() {
            assertTrue(violationsFor("example.com") > 0);
        }

        @DisplayName("Should Reject Blank URL")
        @Test
        void shouldRejectBlankUrl() {
            assertTrue(violationsFor("   ") > 0);
        }

        @DisplayName("Should Reject Empty URL")
        @Test
        void shouldRejectEmptyUrl() {
            assertTrue(violationsFor("") > 0);
        }

        @DisplayName("Should Reject URL With More Than 2048 Characters")
        @Test
        void shouldRejectUrlLongerThanMaxLength() {
            String url = buildUrlWithLength(2049);
            assertEquals(2049, url.length());
            assertTrue(violationsFor(url) > 0);
        }
    }

    private String buildUrlWithLength(int length) {
        String prefix = "https://example.com/";
        return prefix + "a".repeat(length - prefix.length());
    }
}
