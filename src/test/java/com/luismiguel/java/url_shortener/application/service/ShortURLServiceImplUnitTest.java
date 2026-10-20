package com.luismiguel.java.url_shortener.application.service;

import com.luismiguel.java.url_shortener.application.model.ShortURLStats;
import com.luismiguel.java.url_shortener.application.model.ShortenedURLResult;
import com.luismiguel.java.url_shortener.domain.entity.ShortURL;
import com.luismiguel.java.url_shortener.application.port.CodeGenerator;
import com.luismiguel.java.url_shortener.application.port.ShortURLRepository;
import com.luismiguel.java.url_shortener.domain.exception.shorturl.ShortURLNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ShortURLServiceImplUnitTest {
    @Mock
    private ShortURLRepository repository;

    @Mock
    private CodeGenerator codeGenerator;

    @InjectMocks
    private ShortURLServiceImpl service;

    @Nested
    @DisplayName("shortenURL")
    class ShortenURL {
        String originalURL;
        ShortURL url;

        @BeforeEach
        void setUp() {
            originalURL = "https://example.com";
            url = ShortURL.create(originalURL);
        }


        @Test
        @DisplayName("Should Create New Short URL When Original Url Not Found")
        void shouldCreateNewShortURLWhenOriginalUrlNotFound() {
            ReflectionTestUtils.setField(url, "id", 1L);

            when(repository.findByOriginalUrl(originalURL)).thenReturn(Optional.empty());
            when(repository.save(any(ShortURL.class))).thenReturn(url);
            when(codeGenerator.generate(url.getId())).thenReturn("abc123");

            ShortenedURLResult result = service.shortenURL(originalURL);

            assertEquals("abc123", result.shortCode());
            assertTrue(result.created());
            verify(repository).save(any(ShortURL.class));
        }


        @Test
        @DisplayName("Should Return Existing Short URL When Original URL Already Exists")
        void shouldReturnExistingShortURLWhenOriginalURLAlreadyExists() {
            url.assignShortCode("abc123");

            when(repository.findByOriginalUrl(originalURL)).thenReturn(Optional.of(url));

            ShortenedURLResult result = service.shortenURL(originalURL);

            assertEquals("abc123", result.shortCode());
            assertFalse(result.created());
            verify(repository, never()).save(any());
            verify(codeGenerator, never()).generate(any());
        }

        @Test
        @DisplayName("Should Look Up Trimmed Url When Input Has Surrounding Spaces")
        void shouldLookUpTrimmedUrlWhenInputHasSurroundingSpaces() {
            ReflectionTestUtils.setField(url, "id", 1L);

            when(repository.findByOriginalUrl(originalURL)).thenReturn(Optional.empty());
            when(repository.save(any(ShortURL.class))).thenReturn(url);
            when(codeGenerator.generate(url.getId())).thenReturn("abc123");

            ShortenedURLResult result = service.shortenURL("  " + originalURL + "  ");

            assertEquals("abc123", result.shortCode());
            assertTrue(result.created());
            verify(repository).findByOriginalUrl(originalURL);
            verify(repository).save(any(ShortURL.class));
        }

        @Test
        @DisplayName("Should Return Existing Short URL When Input With Spaces Matches Trimmed Url")
        void shouldReturnExistingShortURLWhenInputWithSpacesMatchesTrimmedUrl() {
            url.assignShortCode("abc123");

            when(repository.findByOriginalUrl(originalURL)).thenReturn(Optional.of(url));

            ShortenedURLResult result = service.shortenURL("  " + originalURL + "  ");

            assertEquals("abc123", result.shortCode());
            assertFalse(result.created());
            verify(repository).findByOriginalUrl(originalURL);
            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("Should Acquire Original Url Lock Before Looking Up Original Url")
        void shouldAcquireOriginalUrlLockBeforeLookingUpOriginalUrl() {
            url.assignShortCode("abc123");

            when(repository.findByOriginalUrl(originalURL)).thenReturn(Optional.of(url));

            service.shortenURL(originalURL);

            InOrder inOrder = inOrder(repository);
            inOrder.verify(repository).acquireOriginalUrlLock(originalURL);
            inOrder.verify(repository).findByOriginalUrl(originalURL);
        }
    }

    @Nested
    @DisplayName("getOriginalURL")
    class GetOriginalURL {

        @Test
        @DisplayName("Should Return Original Url When Short Code Exists")
        void shouldReturnOriginalUrlWhenShortCodeExists() {
            String shortCode = "abc123";
            ShortURL shortURL = ShortURL.create("https://example.com");
            shortURL.assignShortCode(shortCode);

            when(repository.findByShortCode(shortCode)).thenReturn(Optional.of(shortURL));

            String result = service.getOriginalURL(shortCode);

            assertEquals("https://example.com", result);
            verify(repository).incrementClickCount(shortCode);
        }


        @Test
        @DisplayName("Should Throw Short URLNotFoundException When Short Code Does Not Exist")
        void shouldThrowShortURLNotFoundExceptionWhenShortCodeDoesNotExist() {
            String shortCode = "nonexistent";

            when(repository.findByShortCode(shortCode)).thenReturn(Optional.empty());

            assertThrows(ShortURLNotFoundException.class, () ->  {
                service.getOriginalURL(shortCode);
            });

            verify(repository, never()).incrementClickCount(any());
        }
    }

    @Nested
    @DisplayName("getShortenedURLStats")
    class GetShortenedURLStats {

        @Test
        @DisplayName("Should Return Stats When Short Code Exists")
        void shouldReturnStatsWhenShortCodeExists() {
            String shortCode = "abc123";
            ShortURL shortURL = ShortURL.create("https://example.com");
            shortURL.assignShortCode(shortCode);

            when(repository.findByShortCode(shortCode)).thenReturn(Optional.of(shortURL));

            ShortURLStats result = service.getShortenedURLStats(shortCode);

            assertEquals(shortCode, result.shortCode());
            assertEquals("https://example.com", result.originalUrl());
            assertEquals(shortURL.getClickCount(), result.clickCount());
        }

        @Test
        @DisplayName("Should Throw ShortURLNotFoundException When Short Code Does Not Exist For Stats")
        void shouldThrowShortURLNotFoundExceptionWhenShortCodeDoesNotExistForStats() {
            when(repository.findByShortCode("nonexistent")).thenReturn(Optional.empty());

            assertThrows(ShortURLNotFoundException.class, () -> {
                service.getShortenedURLStats("nonexistent");
            });
        }
    }
}
