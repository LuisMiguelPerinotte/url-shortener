package com.luismiguel.java.url_shortener.domain.exception.shorturl;

import com.luismiguel.java.url_shortener.domain.exception.category.NotFoundException;

public class ShortURLNotFoundException extends NotFoundException {
    public ShortURLNotFoundException(String shortCode) {
        super("Short URL with shortCode: " + shortCode + ", not found!");
    }
}
