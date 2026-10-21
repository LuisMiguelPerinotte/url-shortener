package com.luismiguel.java.url_shortener.application.service;

import com.luismiguel.java.url_shortener.application.model.ShortURLStats;
import com.luismiguel.java.url_shortener.application.model.ShortenedURLResult;

public interface ShortURLService {
    ShortenedURLResult shortenURL(String originalUrl);

    String getOriginalURL(String shortCode);

    ShortURLStats getShortenedURLStats(String shortCode);
}
