package com.luismiguel.java.url_shortener.web.dto.shorturl.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record ShortenURLRequestDTO(
        @NotBlank
        @URL(regexp = "^https?://.+")
        @Size(max = 2048)
        String url
) {
}
