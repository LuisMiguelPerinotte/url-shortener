package com.luismiguel.java.url_shortener.application.service;

import com.luismiguel.java.url_shortener.web.dto.shortURL.response.GetShortenedURLStatsResponseDTO;
import com.luismiguel.java.url_shortener.web.dto.shortURL.response.ShortenedURLResponseDTO;

public interface ShortURLService {
    ShortenedURLResponseDTO shortenURL(String url);

    String getOriginalURL(String shortCode);

    GetShortenedURLStatsResponseDTO getShortenedURLStats(String shortCode);
}
