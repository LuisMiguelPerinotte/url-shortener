package com.luismiguel.java.url_shortener.domain.entity;

import com.luismiguel.java.url_shortener.infrastructure.exception.business.shortURL.InvalidURLException;
import com.luismiguel.java.url_shortener.infrastructure.exception.business.shortURL.ShortCodeAlreadyAssignedException;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "short_url")
public class ShortURL {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Column(name = "short_code", unique = true)
    private String shortCode;

    @Column(name = "original_url", nullable = false, unique = true)
    private String originalUrl;

    @Column(name = "click_count", nullable = false)
    private Long clickCount;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    public static ShortURL create(String originalUrl) {
        if (originalUrl == null || originalUrl.isBlank())
            throw new InvalidURLException();

        ShortURL url = new ShortURL();
        url.originalUrl = originalUrl.trim();
        url.active = true;
        url.clickCount = 0L;
        return url;
    }

    public void assignShortCode(String code) {
        if (this.shortCode != null)
            throw new ShortCodeAlreadyAssignedException();
        this.shortCode = code;
    }

    public void deactivate() {
        this.active = false;
    }
}
