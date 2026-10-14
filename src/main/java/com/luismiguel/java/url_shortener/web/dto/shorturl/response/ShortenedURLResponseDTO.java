package com.luismiguel.java.url_shortener.web.dto.shorturl.response;

import com.luismiguel.java.url_shortener.application.model.ShortenedURLResult;

public record ShortenedURLResponseDTO(
    String shortCode,
    String originalURL
) {
    public static ShortenedURLResponseDTO from(ShortenedURLResult result) {
        return new ShortenedURLResponseDTO(result.shortCode(), result.originalUrl());
    }
}
