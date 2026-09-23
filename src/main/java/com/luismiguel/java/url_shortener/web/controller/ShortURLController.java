package com.luismiguel.java.url_shortener.web.controller;

import com.luismiguel.java.url_shortener.application.shortURL.ShortURLService;
import com.luismiguel.java.url_shortener.web.dto.shortURL.request.ShortenURLRequestDTO;
import com.luismiguel.java.url_shortener.web.dto.shortURL.response.ShortenedURLResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/api/shortUrl")
public class ShortURLController {
    private final ShortURLService shortURLService;

    @PostMapping
    public ResponseEntity<ShortenedURLResponseDTO> shortenUrl(@RequestBody ShortenURLRequestDTO requestDTO) {
        return new ResponseEntity<>(shortURLService.shortenURL(requestDTO.url()) , HttpStatus.CREATED);
    }
}
