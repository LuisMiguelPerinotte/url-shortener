package com.luismiguel.java.url_shortener.domain.exception.shorturl;

import com.luismiguel.java.url_shortener.domain.exception.category.InvalidInputException;

public class InvalidURLException extends InvalidInputException {
    public InvalidURLException() {
        super("Invalid URL");
    }
}
