package com.luismiguel.java.url_shortener.infrastructure.persistence.repository;

import com.luismiguel.java.url_shortener.application.port.ShortURLRepository;
import com.luismiguel.java.url_shortener.domain.entity.ShortURL;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ShortURLRepositoryAdapter implements ShortURLRepository {
    private final ShortURLJpaRepository jpaRepository;

    @Override
    public ShortURL save(ShortURL shortURL) {
        return jpaRepository.save(shortURL);
    }

    @Override
    public Optional<ShortURL> findByShortCode(String shortCode) {
        return jpaRepository.findByShortCode(shortCode);
    }

    @Override
    public Optional<ShortURL> findByOriginalUrl(String originalUrl) {
        return jpaRepository.findByOriginalUrl(originalUrl);
    }

    @Override
    public void incrementClickCount(String shortCode) {
        jpaRepository.incrementClickCount(shortCode);
    }

    @Override
    public void acquireOriginalUrlLock(String originalUrl) {
        jpaRepository.acquireOriginalUrlLock(originalUrl);
    }
}
