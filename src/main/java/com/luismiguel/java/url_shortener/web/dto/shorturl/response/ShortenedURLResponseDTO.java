package com.luismiguel.java.url_shortener.web.dto.shorturl.response;

import com.luismiguel.java.url_shortener.application.model.ShortenedURLResult;
import io.swagger.v3.oas.annotations.media.Schema;

public record ShortenedURLResponseDTO(
    @Schema(description = "Code that identifies the short URL", example = "DQm5Q0")
    String shortCode,
    @Schema(description = "The original URL that was shortened", example = "https://example.com/some/very/long/path")
    String originalURL
) {
    public static ShortenedURLResponseDTO from(ShortenedURLResult result) {
        return new ShortenedURLResponseDTO(result.shortCode(), result.originalUrl());
    }
}
