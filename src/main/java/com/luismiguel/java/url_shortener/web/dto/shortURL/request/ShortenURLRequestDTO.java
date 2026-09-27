package com.luismiguel.java.url_shortener.web.dto.shortURL.request;


import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public record ShortenURLRequestDTO(
        @NotBlank
        @URL
        String url
) {
}
