package com.luismiguel.java.url_shortener.infrastructure.exception.business.shortURL;

import org.springframework.http.HttpStatus;

public class ShortURLNotFoundException extends ShortURLException {
    public ShortURLNotFoundException(String shortCode) {
        super("Short URL with shortCode: " + shortCode + ", not found!", HttpStatus.NOT_FOUND);
    }
}
