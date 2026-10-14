package com.luismiguel.java.url_shortener.web.controller;

import com.luismiguel.java.url_shortener.application.service.ShortURLService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class RedirectController {
    private final ShortURLService shortURLService;

    @GetMapping("/{code}")
    public ResponseEntity<Void> redirect(@PathVariable String code) {
        String originalURL = shortURLService.getOriginalURL(code);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(originalURL));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
}
