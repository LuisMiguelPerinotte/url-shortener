package com.luismiguel.java.url_shortener.domain.entity;

import com.luismiguel.java.url_shortener.domain.exception.shorturl.InvalidURLException;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.net.URI;
import java.net.URISyntaxException;
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

    @Column(name = "short_code", unique = true, length = 10)
    private String shortCode;

    // uniqueness is enforced by the functional index ux_short_url_original_url_md5 (V3), not by a column constraint
    @Column(name = "original_url", nullable = false)
    private String originalUrl;

    @Column(name = "click_count", nullable = false)
    private Long clickCount;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    public static String normalize(String url) {
        if (url == null)
            return null;
        return url.trim();
    }

    public static ShortURL create(String originalUrl) {
        if (originalUrl == null || originalUrl.isBlank())
            throw new InvalidURLException();

        String normalized = normalize(originalUrl);
        try {
            new URI(normalized);

        } catch (URISyntaxException e) {
            throw new InvalidURLException();
        }

        ShortURL url = new ShortURL();
        url.originalUrl = normalized;
        url.active = true;
        url.clickCount = 0L;
        return url;
    }

    public void assignShortCode(String code) {
        if (this.shortCode != null)
            throw new IllegalStateException("Short code already assigned");
        this.shortCode = code;
    }

    public void deactivate() {
        this.active = false;
    }
}
