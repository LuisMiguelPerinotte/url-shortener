package com.luismiguel.java.url_shortener.infrastructure.exception.business.shortURL;

import org.springframework.http.HttpStatus;

public class InvalidURLException extends ShortURLException {
    public InvalidURLException() {
        super("Invalid URL", HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
