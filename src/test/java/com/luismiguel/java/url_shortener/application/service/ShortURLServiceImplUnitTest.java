package com.luismiguel.java.url_shortener.application.service;

import com.luismiguel.java.url_shortener.domain.entity.ShortURL;
import com.luismiguel.java.url_shortener.infrastructure.codeGenerator.CodeGenerator;
import com.luismiguel.java.url_shortener.infrastructure.exception.business.shortURL.ShortURLNotFoundException;
import com.luismiguel.java.url_shortener.infrastructure.persistence.repository.ShortURLRepository;
import com.luismiguel.java.url_shortener.web.dto.shortURL.response.GetShortenedURLStatsResponseDTO;
import com.luismiguel.java.url_shortener.web.dto.shortURL.response.ShortenedURLResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

            ShortenedURLResponseDTO result = service.shortenURL(originalURL);

            assertEquals("abc123", result.shortCode());
            verify(repository).save(any(ShortURL.class));
        }


        @Test
        @DisplayName("Should Return Existing Short URL When Original URL Already Exists")
        void shouldReturnExistingShortURLWhenOriginalURLAlreadyExists() {
            url.assignShortCode("abc123");

            when(repository.findByOriginalUrl(originalURL)).thenReturn(Optional.of(url));

            ShortenedURLResponseDTO result = service.shortenURL(originalURL);

            assertEquals("abc123", result.shortCode());
            verify(repository, never()).save(any());
            verify(codeGenerator, never()).generate(any());
        }
    }

    @Nested
    @DisplayName("getOriginalURL")
    class GetOriginalURL {

        @Test
        @DisplayName("")
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

            GetShortenedURLStatsResponseDTO result = service.getShortenedURLStats(shortCode);

            assertEquals(shortCode, result.shortCode());
            assertEquals("https://example.com", result.originalURL());
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
