package com.luismiguel.java.url_shortener.web.dto.shorturl.request;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record ShortenURLRequestDTO(
        @Schema(description = "Original URL to shorten. Must be an http or https URL, up to 2048 characters.",
                example = "https://example.com/some/very/long/path")
        @NotBlank
        @URL(regexp = "^https?://.+")
        @Size(max = 2048)
        String url
) {
}
