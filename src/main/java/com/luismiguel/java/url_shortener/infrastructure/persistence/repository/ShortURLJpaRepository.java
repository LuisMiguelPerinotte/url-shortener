package com.luismiguel.java.url_shortener.infrastructure.persistence.repository;

import com.luismiguel.java.url_shortener.domain.entity.ShortURL;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface ShortURLJpaRepository extends JpaRepository<ShortURL, Long> {
    Optional<ShortURL> findByShortCode(String shortCode);

    @Query(value = """
    SELECT * FROM short_url
    WHERE md5(original_url) = md5(:originalURL)
      AND original_url = :originalURL
""", nativeQuery = true)
    Optional<ShortURL> findByOriginalUrl(String originalURL);

    /** Serializes find-or-create per URL. Must be the first statement of a READ COMMITTED
     *  transaction; released automatically on commit/rollback. */
    @Transactional(propagation = Propagation.MANDATORY)
    @Query(value = "SELECT 1 FROM pg_advisory_xact_lock(hashtextextended(:url, 0))", nativeQuery = true)
    Integer acquireOriginalUrlLock(String url);

    @Modifying
    @Query("""
    UPDATE ShortURL s
    SET s.clickCount = s.clickCount + 1
    WHERE s.shortCode = :shortCode
""")
    void incrementClickCount(String shortCode);
}
