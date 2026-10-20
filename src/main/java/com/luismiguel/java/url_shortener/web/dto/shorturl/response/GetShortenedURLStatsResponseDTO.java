package com.luismiguel.java.url_shortener.web.dto.shorturl.response;

import com.luismiguel.java.url_shortener.application.model.ShortURLStats;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record GetShortenedURLStatsResponseDTO(
        @Schema(description = "Code that identifies the short URL", example = "DQm5Q0")
        String shortCode,
        @Schema(description = "The original URL", example = "https://example.com/some/very/long/path")
        String originalURL,
        @Schema(description = "Number of times the short URL was accessed", example = "42")
        Long clickCount,
        @Schema(description = "Creation date and time (local, without offset)", example = "2026-10-20T12:00:00")
        LocalDateTime createdAt
) {
    public static GetShortenedURLStatsResponseDTO from(ShortURLStats stats) {
        return new GetShortenedURLStatsResponseDTO(
                stats.shortCode(),
                stats.originalUrl(),
                stats.clickCount(),
                stats.createdAt()
        );
    }
}
