package com.luismiguel.java.url_shortener.application.port;

import com.luismiguel.java.url_shortener.domain.entity.ShortURL;

import java.util.Optional;

public interface ShortURLRepository {
    ShortURL save(ShortURL shortURL);

    Optional<ShortURL> findByShortCode(String shortCode);

    Optional<ShortURL> findByOriginalUrl(String originalUrl);

    void incrementClickCount(String shortCode);

    /** Serializes find-or-create per URL; must be called first in a READ COMMITTED transaction. */
    void acquireOriginalUrlLock(String originalUrl);
}
