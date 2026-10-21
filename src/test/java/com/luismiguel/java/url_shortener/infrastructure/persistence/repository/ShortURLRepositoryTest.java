package com.luismiguel.java.url_shortener.infrastructure.persistence.repository;

import com.luismiguel.java.url_shortener.TestcontainersConfiguration;
import com.luismiguel.java.url_shortener.application.port.ShortURLRepository;
import com.luismiguel.java.url_shortener.domain.entity.ShortURL;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class ShortURLRepositoryTest {

    @Autowired
    private ShortURLRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("Should find only the exact original URL, not a case-changed variant")
    void shouldFindOnlyExactOriginalUrl() {
        String url = "https://example.com/" + UUID.randomUUID() + "/page";
        save(url, newShortCode());
        entityManager.flush();

        assertTrue(repository.findByOriginalUrl(url).isPresent());
        assertFalse(repository.findByOriginalUrl("https://example.com/" + UUID.randomUUID()).isPresent());
        assertFalse(repository.findByOriginalUrl(url.replace("/page", "/Page")).isPresent());
    }

    @Test
    @DisplayName("Should increment click count only for the given short code")
    void shouldIncrementClickCountOnlyForGivenShortCode() {
        String codeA = newShortCode();
        String codeB = newShortCode();
        save("https://example.com/" + UUID.randomUUID(), codeA);
        save("https://example.com/" + UUID.randomUUID(), codeB);
        entityManager.flush();

        repository.incrementClickCount(codeA);
        repository.incrementClickCount(codeA);
        entityManager.flush();
        entityManager.clear();

        assertEquals(2L, repository.findByShortCode(codeA).orElseThrow().getClickCount());
        assertEquals(0L, repository.findByShortCode(codeB).orElseThrow().getClickCount());
    }

    @Test
    @DisplayName("Should reject a second row with the same original URL via the V3 md5 unique index")
    void shouldRejectDuplicateOriginalUrlByUniqueIndex() {
        String url = "https://example.com/" + UUID.randomUUID();
        save(url, newShortCode());

        assertThrows(DataIntegrityViolationException.class, () -> {
            save(url, newShortCode());
            entityManager.flush();
        });
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @DisplayName("Should refuse to take the original URL lock outside a transaction")
    void shouldRefuseLockOutsideTransaction() {
        assertThrows(IllegalTransactionStateException.class,
                () -> repository.acquireOriginalUrlLock("https://example.com/x"));
    }

    // helpers

    private void save(String url, String shortCode) {
        ShortURL shortURL = ShortURL.create(url);
        shortURL.assignShortCode(shortCode);
        repository.save(shortURL);
    }

    private String newShortCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}
