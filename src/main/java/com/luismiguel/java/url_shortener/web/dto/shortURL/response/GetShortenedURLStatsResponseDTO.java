package com.luismiguel.java.url_shortener.web.dto.shortURL.response;

import java.time.LocalDateTime;

public record GetShortenedURLStatsResponseDTO(
        String shortCode,
        String originalURL,
        Long clickCount,
        LocalDateTime createdAt
) {
}
