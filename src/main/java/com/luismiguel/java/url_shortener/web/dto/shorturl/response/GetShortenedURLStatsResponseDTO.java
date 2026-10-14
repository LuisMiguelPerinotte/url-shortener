package com.luismiguel.java.url_shortener.web.dto.shorturl.response;

import com.luismiguel.java.url_shortener.application.model.ShortURLStats;

import java.time.LocalDateTime;

public record GetShortenedURLStatsResponseDTO(
        String shortCode,
        String originalURL,
        Long clickCount,
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
