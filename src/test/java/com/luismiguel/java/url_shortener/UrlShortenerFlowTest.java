package com.luismiguel.java.url_shortener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class UrlShortenerFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should shorten, redirect, count the click, report stats and return 404 for unknown code")
    void shouldCompleteShortenRedirectStatsFlow() throws Exception {
        String url = "https://example.com/" + UUID.randomUUID();
        String body = "{\"url\":\"" + url + "\"}";

        // (a) first shortening creates the row
        MvcResult created = mockMvc.perform(post("/api/v1/short-url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortCode").isNotEmpty())
                .andExpect(jsonPath("$.originalURL").value(url))
                .andReturn();
        String shortCode = readField(created, "shortCode");

        // (b) same URL again is deduplicated
        mockMvc.perform(post("/api/v1/short-url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortCode").value(shortCode));

        // (c) redirect to the original URL
        mockMvc.perform(get("/" + shortCode))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", url))
                .andExpect(content().string(""));

        // (d) stats reflect the single click
        mockMvc.perform(get("/api/v1/short-url/" + shortCode + "/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortCode").value(shortCode))
                .andExpect(jsonPath("$.originalURL").value(url))
                .andExpect(jsonPath("$.clickCount").value(1))
                .andExpect(jsonPath("$.createdAt").exists());

        // (e) unknown code
        mockMvc.perform(get("/nope123"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    private String readField(MvcResult result, String field) throws Exception {
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        String value = node.get(field).asText();
        assertFalse(value.isBlank(), "field " + field + " is blank");
        return value;
    }
}
