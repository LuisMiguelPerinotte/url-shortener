package com.luismiguel.java.url_shortener.web.controller;

import com.luismiguel.java.url_shortener.application.model.ShortenedURLResult;
import com.luismiguel.java.url_shortener.application.service.ShortURLService;
import com.luismiguel.java.url_shortener.infrastructure.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShortURLController.class)
@Import(GlobalExceptionHandler.class)
class ShortURLControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShortURLService shortURLService;

    @Test
    @DisplayName("Should Return 201 With Body When Short URL Is Created")
    void shouldReturn201WhenShortURLIsCreated() throws Exception {
        when(shortURLService.shortenURL(anyString()))
                .thenReturn(new ShortenedURLResult("abc123", "https://example.com", true));

        mockMvc.perform(shortenRequest("{\"url\":\"https://example.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortCode").value("abc123"))
                .andExpect(jsonPath("$.originalURL").value("https://example.com"));
    }

    @Test
    @DisplayName("Should Return 200 With Body When Short URL Already Exists")
    void shouldReturn200WhenShortURLAlreadyExists() throws Exception {
        when(shortURLService.shortenURL(anyString()))
                .thenReturn(new ShortenedURLResult("abc123", "https://example.com", false));

        mockMvc.perform(shortenRequest("{\"url\":\"https://example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortCode").value("abc123"))
                .andExpect(jsonPath("$.originalURL").value("https://example.com"));
    }

    @Test
    @DisplayName("Should Return 400 When URL Scheme Is Not Http Or Https")
    void shouldReturn400WhenURLSchemeIsNotHttpOrHttps() throws Exception {
        mockMvc.perform(shortenRequest("{\"url\":\"ftp://x\"}"))
                .andExpect(status().isBadRequest());
    }

    private MockHttpServletRequestBuilder shortenRequest(String body) {
        return post("/api/v1/short-url")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body);
    }
}
