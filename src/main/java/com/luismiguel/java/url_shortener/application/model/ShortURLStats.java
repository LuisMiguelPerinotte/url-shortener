package com.luismiguel.java.url_shortener.application.model;

import java.time.LocalDateTime;

public record ShortURLStats(
        String shortCode,
        String originalUrl,
        Long clickCount,
        LocalDateTime createdAt
) {
}
