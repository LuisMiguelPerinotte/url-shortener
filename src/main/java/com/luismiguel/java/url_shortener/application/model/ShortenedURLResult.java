package com.luismiguel.java.url_shortener.application.model;

public record ShortenedURLResult(
        String shortCode,
        String originalUrl,
        boolean created
) {
}
