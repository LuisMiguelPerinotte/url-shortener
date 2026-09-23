package com.luismiguel.java.url_shortener.web.controller;

import com.luismiguel.java.url_shortener.application.shortURL.ShortURLService;
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

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        String originalURL = shortURLService.getOriginalURL(shortCode);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(originalURL));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
}
