package com.luismiguel.java.url_shortener.web.controller;

import com.luismiguel.java.url_shortener.application.service.ShortURLService;
import com.luismiguel.java.url_shortener.web.dto.shortURL.request.ShortenURLRequestDTO;
import com.luismiguel.java.url_shortener.web.dto.shortURL.response.GetShortenedURLStatsResponseDTO;
import com.luismiguel.java.url_shortener.web.dto.shortURL.response.ShortenedURLResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/short-url")
public class ShortURLController {
    private final ShortURLService shortURLService;

    @PostMapping
    public ResponseEntity<ShortenedURLResponseDTO> shortenUrl(@Valid @RequestBody ShortenURLRequestDTO requestDTO) {
        return new ResponseEntity<>(shortURLService.shortenURL(requestDTO.url()) , HttpStatus.CREATED);
    }

    @GetMapping("/{code}/stats")
    public ResponseEntity<GetShortenedURLStatsResponseDTO> getShortenedURLStats(@PathVariable String code) {
        return new ResponseEntity<>(shortURLService.getShortenedURLStats(code), HttpStatus.OK);
    }
}
