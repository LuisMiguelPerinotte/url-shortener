package com.luismiguel.java.url_shortener.infrastructure.persistence.shortURL;

import com.luismiguel.java.url_shortener.domain.shortURL.ShortURL;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShortURLRepository extends JpaRepository<ShortURL, Long> {
    Optional<ShortURL> findByShortCode(String shortCode);
}
