package com.luismiguel.java.url_shortener.infrastructure.exception.business.shortURL;

import com.luismiguel.java.url_shortener.infrastructure.exception.business.BusinessException;
import org.springframework.http.HttpStatus;

public class ShortURLException extends BusinessException {
    public ShortURLException(String message, HttpStatus status) {
        super(message, status);
    }
}
