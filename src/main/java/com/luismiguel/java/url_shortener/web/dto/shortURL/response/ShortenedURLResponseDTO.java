package com.luismiguel.java.url_shortener.web.dto.shortURL.response;

public record ShortenedURLResponseDTO(
    String shortCode,
    String originalURL
) {
}
