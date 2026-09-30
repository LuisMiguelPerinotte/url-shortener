package com.luismiguel.java.url_shortener.infrastructure.persistence.repository;

import com.luismiguel.java.url_shortener.domain.entity.ShortURL;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShortURLRepository extends JpaRepository<ShortURL, Long> {
    Optional<ShortURL> findByShortCode(String shortCode);

    Optional<ShortURL> findByOriginalUrl(String originalURL);

    @Modifying
    @Query("""
    UPDATE ShortURL s
    SET s.clickCount = s.clickCount + 1
    WHERE s.shortCode = :shortCode
""")
    void incrementClickCount(String shortCode);
}
