package com.luismiguel.java.url_shortener.infrastructure.codeGenerator;

import org.springframework.stereotype.Component;

@Component
public class Base62CodeGenerator implements CodeGenerator {
    private static final String CHARACTERS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    @Override
    public String generate(Long id) {
        if (id == 0) {
            return "0";
        }

        StringBuilder code = new StringBuilder();

        while (id > 0) {
            int resto = (int) (id % 62);
            code.append(CHARACTERS.charAt(resto));
            id /= 62;
        }

        return code.reverse().toString();
    }
}
