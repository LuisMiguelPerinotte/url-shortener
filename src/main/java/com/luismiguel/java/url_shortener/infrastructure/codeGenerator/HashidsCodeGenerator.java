package com.luismiguel.java.url_shortener.infrastructure.codeGenerator;

import org.hashids.Hashids;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HashidsCodeGenerator implements CodeGenerator {
    private final Hashids hashids;

    public HashidsCodeGenerator(
            @Value("${app.hashids.salt}") String salt,
            @Value("${app.hashids.min-length}") int minLength) {
        this.hashids = new Hashids(salt, minLength);
    }

    @Override
    public String generate(Long id) {
        return hashids.encode(id);
    }
}
