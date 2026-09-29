package com.luismiguel.java.url_shortener.application.shortURL;

import com.luismiguel.java.url_shortener.domain.shortURL.ShortURL;
import com.luismiguel.java.url_shortener.infrastructure.codeGenerator.CodeGenerator;
import com.luismiguel.java.url_shortener.infrastructure.exception.business.shortURL.ShortURLNotFoundException;
import com.luismiguel.java.url_shortener.infrastructure.persistence.shortURL.ShortURLRepository;
import com.luismiguel.java.url_shortener.web.dto.shortURL.response.GetShortenedURLStatsResponseDTO;
import com.luismiguel.java.url_shortener.web.dto.shortURL.response.ShortenedURLResponseDTO;
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
    public ShortenedURLResponseDTO shortenURL(String url) {
        ShortURL shortURL = findOrCreate(url);
        return new ShortenedURLResponseDTO(shortURL.getShortCode(), shortURL.getOriginalUrl());
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
    public GetShortenedURLStatsResponseDTO getShortenedURLStats(String shortCode) {
        ShortURL shortURL = shortURLRepository.findByShortCode(shortCode).
                orElseThrow(() -> new ShortURLNotFoundException(shortCode));

        return new GetShortenedURLStatsResponseDTO(
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
