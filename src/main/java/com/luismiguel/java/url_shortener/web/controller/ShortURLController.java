package com.luismiguel.java.url_shortener.web.controller;

import com.luismiguel.java.url_shortener.application.model.ShortenedURLResult;
import com.luismiguel.java.url_shortener.application.service.ShortURLService;
import com.luismiguel.java.url_shortener.web.dto.shorturl.request.ShortenURLRequestDTO;
import com.luismiguel.java.url_shortener.web.dto.shorturl.response.GetShortenedURLStatsResponseDTO;
import com.luismiguel.java.url_shortener.web.dto.shorturl.response.ShortenedURLResponseDTO;
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
        ShortenedURLResult result = shortURLService.shortenURL(requestDTO.url());
        HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;
        return new ResponseEntity<>(ShortenedURLResponseDTO.from(result), status);
    }

    @GetMapping("/{code}/stats")
    public ResponseEntity<GetShortenedURLStatsResponseDTO> getShortenedURLStats(@PathVariable String code) {
        return new ResponseEntity<>(GetShortenedURLStatsResponseDTO.from(shortURLService.getShortenedURLStats(code)), HttpStatus.OK);
    }
}
