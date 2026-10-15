package com.luismiguel.java.url_shortener.domain.exception.category;

import com.luismiguel.java.url_shortener.domain.exception.DomainException;

public abstract class InvalidInputException extends DomainException {
    protected InvalidInputException(String message) {
        super(message);
    }
}
