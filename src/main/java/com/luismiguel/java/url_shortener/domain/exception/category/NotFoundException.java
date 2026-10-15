package com.luismiguel.java.url_shortener.domain.exception.category;

import com.luismiguel.java.url_shortener.domain.exception.DomainException;

public abstract class NotFoundException extends DomainException {
    protected NotFoundException(String message) {
        super(message);
    }
}
