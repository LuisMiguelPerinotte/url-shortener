package com.luismiguel.java.url_shortener.infrastructure.exception;

import com.luismiguel.java.url_shortener.domain.exception.shorturl.InvalidURLException;
import com.luismiguel.java.url_shortener.domain.exception.category.NotFoundException;
import com.luismiguel.java.url_shortener.domain.exception.shorturl.ShortURLNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new FakeController())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    @DisplayName("Should Return 404 With Error Body When Short URL Not Found")
    void shouldReturn404WhenShortURLNotFound() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Short URL with shortCode: abc, not found!"))
                .andExpect(jsonPath("$.path").value("/test/not-found"));
    }

    @Test
    @DisplayName("Should Return 422 With Error Body When URL Is Invalid")
    void shouldReturn422WhenURLIsInvalid() throws Exception {
        mockMvc.perform(get("/test/invalid-url"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.error").value("UNPROCESSABLE_ENTITY"))
                .andExpect(jsonPath("$.message").value("Invalid URL"))
                .andExpect(jsonPath("$.path").value("/test/invalid-url"));
    }

    @Test
    @DisplayName("Should Return 404 For Any NotFoundException Subclass Without A Dedicated Handler")
    void shouldReturn404ForAnyNotFoundCategoryException() throws Exception {
        mockMvc.perform(get("/test/category-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Custom category not found"))
                .andExpect(jsonPath("$.path").value("/test/category-not-found"));
    }

    @Test
    @DisplayName("Should Return 500 With Generic Message When Unexpected Error Occurs")
    void shouldReturn500WhenUnexpectedErrorOccurs() throws Exception {
        mockMvc.perform(get("/test/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred."))
                .andExpect(jsonPath("$.path").value("/test/unexpected"));
    }

    @Test
    @DisplayName("Should Return 415 Without Null In Message When Content Type Is Missing")
    void shouldReturn415WithoutNullWhenContentTypeIsMissing() throws Exception {
        mockMvc.perform(post("/test/json-only").content("{}"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value(415))
                .andExpect(jsonPath("$.error").value("UNSUPPORTED_MEDIA_TYPE"))
                .andExpect(jsonPath("$.message").value("Content type is missing"))
                .andExpect(jsonPath("$.path").value("/test/json-only"));
    }

    @RestController
    static class FakeController {
        @PostMapping(value = "/test/json-only", consumes = "application/json")
        void jsonOnly() {
        }

        @GetMapping("/test/not-found")
        void notFound() {
            throw new ShortURLNotFoundException("abc");
        }

        @GetMapping("/test/invalid-url")
        void invalidUrl() {
            throw new InvalidURLException();
        }

        @GetMapping("/test/unexpected")
        void unexpected() {
            throw new IllegalStateException("Short code already assigned");
        }

        @GetMapping("/test/category-not-found")
        void categoryNotFound() {
            throw new CustomCategoryNotFoundException();
        }
    }

    static class CustomCategoryNotFoundException extends NotFoundException {
        CustomCategoryNotFoundException() {
            super("Custom category not found");
        }
    }
}
