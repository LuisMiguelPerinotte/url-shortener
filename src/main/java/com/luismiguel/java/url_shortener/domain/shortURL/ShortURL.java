package com.luismiguel.java.url_shortener.domain.shortURL;

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

    @Column(name = "short_code", nullable = false, unique = true)
    private String shortCode;

    @Column(name = "original_url", nullable = false)
    private String originalUrl;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    public static ShortURL create(String originalUrl) {
        ShortURL url = new ShortURL();
        url.originalUrl = originalUrl;
        url.active = true;
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
