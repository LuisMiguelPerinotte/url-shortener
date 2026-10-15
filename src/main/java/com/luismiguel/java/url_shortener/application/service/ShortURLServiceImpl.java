package com.luismiguel.java.url_shortener.application.service;

import com.luismiguel.java.url_shortener.application.model.ShortURLStats;
import com.luismiguel.java.url_shortener.application.model.ShortenedURLResult;
import com.luismiguel.java.url_shortener.application.port.CodeGenerator;
import com.luismiguel.java.url_shortener.domain.entity.ShortURL;
import com.luismiguel.java.url_shortener.domain.exception.shorturl.ShortURLNotFoundException;
import com.luismiguel.java.url_shortener.infrastructure.persistence.repository.ShortURLRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShortURLServiceImpl implements ShortURLService {
    private final ShortURLRepository shortURLRepository;
    private final CodeGenerator codeGenerator;

    @Override
    @Transactional
    public ShortenedURLResult shortenURL(String originalUrl) {
        ShortURL shortURL = findOrCreate(originalUrl);
        return new ShortenedURLResult(shortURL.getShortCode(), shortURL.getOriginalUrl());
    }

    @Override
    @Transactional
    public String getOriginalURL(String shortCode) {
        ShortURL shortURL = shortURLRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ShortURLNotFoundException(shortCode));

        shortURLRepository.incrementClickCount(shortCode);
        return shortURL.getOriginalUrl();
    }

    @Override
    public ShortURLStats getShortenedURLStats(String shortCode) {
        ShortURL shortURL = shortURLRepository.findByShortCode(shortCode).
                orElseThrow(() -> new ShortURLNotFoundException(shortCode));

        return new ShortURLStats(
                shortURL.getShortCode(),
                shortURL.getOriginalUrl(),
                shortURL.getClickCount(),
                shortURL.getCreatedAt()
        );
    }

    // private methods
    private ShortURL findOrCreate(String url) {
        return shortURLRepository.findByOriginalUrl(url)
                .orElseGet(() -> createNewShortURL(url));
    }

    private ShortURL createNewShortURL(String url) {
        ShortURL savedURL = shortURLRepository.save(ShortURL.create(url));
        String shortCode = codeGenerator.generate(savedURL.getId());
        savedURL.assignShortCode(shortCode);

        return savedURL;
    }


}
