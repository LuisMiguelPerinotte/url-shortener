package com.luismiguel.java.url_shortener.web.controller;

import com.luismiguel.java.url_shortener.application.model.ShortenedURLResult;
import com.luismiguel.java.url_shortener.application.service.ShortURLService;
import com.luismiguel.java.url_shortener.web.dto.shorturl.request.ShortenURLRequestDTO;
import com.luismiguel.java.url_shortener.web.dto.shorturl.response.GetShortenedURLStatsResponseDTO;
import com.luismiguel.java.url_shortener.web.dto.shorturl.response.ShortenedURLResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/short-url")
@Tag(name = "Short URL", description = "Create short URLs and read their statistics")
public class ShortURLController {
    private final ShortURLService shortURLService;

    @PostMapping
    @Operation(
            summary = "Shorten a URL",
            description = "Creates a short code for the given http/https URL. If the URL was already shortened, "
                    + "the existing short code is returned with status 200 instead of 201."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "A new short URL was created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ShortenedURLResponseDTO.class))),
            @ApiResponse(
                    responseCode = "200",
                    description = "The URL was already shortened; the existing short URL is returned",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ShortenedURLResponseDTO.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failed (blank URL, not http/https, or longer than 2048 characters)",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "Validation error",
                            value = """
                                    {
                                      "timestamp": "2026-10-20T12:00:00Z",
                                      "status": 400,
                                      "error": "BAD_REQUEST",
                                      "message": "url: must be a valid URL",
                                      "path": "/api/v1/short-url"
                                    }"""))),
            @ApiResponse(
                    responseCode = "415",
                    description = "Missing or unsupported Content-Type (use application/json)",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "Unsupported media type",
                            value = """
                                    {
                                      "timestamp": "2026-10-20T12:00:00Z",
                                      "status": 415,
                                      "error": "UNSUPPORTED_MEDIA_TYPE",
                                      "message": "Content type text/plain is not supported",
                                      "path": "/api/v1/short-url"
                                    }"""))),
            @ApiResponse(
                    responseCode = "422",
                    description = "The URL passes the format check but cannot be parsed as a URI",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "Invalid URL",
                            value = """
                                    {
                                      "timestamp": "2026-10-20T12:00:00Z",
                                      "status": 422,
                                      "error": "UNPROCESSABLE_ENTITY",
                                      "message": "Invalid URL",
                                      "path": "/api/v1/short-url"
                                    }"""))),
            @ApiResponse(
                    responseCode = "429",
                    description = "Too many requests from this IP (10 per minute by default). "
                            + "The Retry-After header gives the number of seconds to wait",
                    headers = @Header(
                            name = HttpHeaders.RETRY_AFTER,
                            description = "Seconds to wait before retrying",
                            schema = @Schema(type = "integer", minimum = "1")),
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "Rate limit exceeded",
                            value = """
                                    {
                                      "timestamp": "2026-10-20T12:00:00Z",
                                      "status": 429,
                                      "error": "TOO_MANY_REQUESTS",
                                      "message": "Too many requests",
                                      "path": "/api/v1/short-url"
                                    }"""))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected server error",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "Unexpected error",
                            value = """
                                    {
                                      "timestamp": "2026-10-20T12:00:00Z",
                                      "status": 500,
                                      "error": "INTERNAL_SERVER_ERROR",
                                      "message": "An unexpected error occurred.",
                                      "path": "/api/v1/short-url"
                                    }""")))
    })
    public ResponseEntity<ShortenedURLResponseDTO> shortenUrl(@Valid @RequestBody ShortenURLRequestDTO requestDTO) {
        ShortenedURLResult result = shortURLService.shortenURL(requestDTO.url());
        HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;
        return new ResponseEntity<>(ShortenedURLResponseDTO.from(result), status);
    }

    @GetMapping("/{code}/stats")
    @Operation(
            summary = "Get short URL statistics",
            description = "Returns the original URL, the number of clicks and the creation date of a short code."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Statistics of the short URL",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = GetShortenedURLStatsResponseDTO.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "No short URL exists for this code",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "Not found",
                            value = """
                                    {
                                      "timestamp": "2026-10-20T12:00:00Z",
                                      "status": 404,
                                      "error": "NOT_FOUND",
                                      "message": "Short URL with shortCode: DQm5Q0, not found!",
                                      "path": "/api/v1/short-url/DQm5Q0/stats"
                                    }"""))),
            @ApiResponse(
                    responseCode = "429",
                    description = "Too many requests from this IP (120 per minute by default). "
                            + "The Retry-After header gives the number of seconds to wait",
                    headers = @Header(
                            name = HttpHeaders.RETRY_AFTER,
                            description = "Seconds to wait before retrying",
                            schema = @Schema(type = "integer", minimum = "1")),
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "Rate limit exceeded",
                            value = """
                                    {
                                      "timestamp": "2026-10-20T12:00:00Z",
                                      "status": 429,
                                      "error": "TOO_MANY_REQUESTS",
                                      "message": "Too many requests",
                                      "path": "/api/v1/short-url/DQm5Q0/stats"
                                    }"""))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected server error",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "Unexpected error",
                            value = """
                                    {
                                      "timestamp": "2026-10-20T12:00:00Z",
                                      "status": 500,
                                      "error": "INTERNAL_SERVER_ERROR",
                                      "message": "An unexpected error occurred.",
                                      "path": "/api/v1/short-url/DQm5Q0/stats"
                                    }""")))
    })
    public ResponseEntity<GetShortenedURLStatsResponseDTO> getShortenedURLStats(
            @Parameter(description = "Short code returned by the shorten endpoint", example = "DQm5Q0")
            @PathVariable String code) {
        return new ResponseEntity<>(GetShortenedURLStatsResponseDTO.from(shortURLService.getShortenedURLStats(code)), HttpStatus.OK);
    }
}
