package com.luismiguel.java.url_shortener.web.controller;

import com.luismiguel.java.url_shortener.application.service.ShortURLService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Redirect", description = "Redirects a short code to its original URL")
public class RedirectController {
    private final ShortURLService shortURLService;

    @GetMapping("/{code}")
    @Operation(
            summary = "Redirect to the original URL",
            description = "Responds with 302 and a Location header pointing to the original URL, and counts the click."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "302",
                    description = "Redirect to the original URL",
                    headers = @Header(
                            name = HttpHeaders.LOCATION,
                            description = "The original URL of the short code",
                            schema = @Schema(type = "string", format = "uri"))),
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
                                      "path": "/DQm5Q0"
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
                                      "path": "/DQm5Q0"
                                    }""")))
    })
    public ResponseEntity<Void> redirect(
            @Parameter(description = "Short code returned by the shorten endpoint", example = "DQm5Q0")
            @PathVariable String code) {
        String originalURL = shortURLService.getOriginalURL(code);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(originalURL));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
}
