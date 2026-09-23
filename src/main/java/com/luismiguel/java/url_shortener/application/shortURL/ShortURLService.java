package com.luismiguel.java.url_shortener.application.shortURL;

import com.luismiguel.java.url_shortener.web.dto.shortURL.response.ShortenedURLResponseDTO;
import org.springframework.stereotype.Service;

@Service
public interface ShortURLService {
    ShortenedURLResponseDTO shortenURL(String url);

    String getOriginalURL(String shortCode);
}
