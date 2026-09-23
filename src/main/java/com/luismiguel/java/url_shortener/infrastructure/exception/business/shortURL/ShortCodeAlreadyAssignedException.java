package com.luismiguel.java.url_shortener.infrastructure.exception.business.shortURL;

import org.springframework.http.HttpStatus;

public class ShortCodeAlreadyAssignedException extends ShortURLException {
    public ShortCodeAlreadyAssignedException() {
        super("Code already assigned", HttpStatus.CONFLICT);
    }
}
