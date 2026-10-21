package com.luismiguel.java.url_shortener.web;

import com.luismiguel.java.url_shortener.TestcontainersConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class OpenApiDocsTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should expose the OpenAPI document with all API paths and responses")
    void shouldExposeOpenApiDocument() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/short-url']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/short-url'].post.responses['201']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/short-url'].post.responses['200']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/short-url/{code}/stats']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/short-url/{code}/stats'].get.responses['200']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/short-url/{code}/stats'].get.responses['404']").exists())
                .andExpect(jsonPath("$.paths['/{code}']").exists())
                .andExpect(jsonPath("$.paths['/{code}'].get.responses['302']").exists())
                .andExpect(jsonPath("$.paths['/{code}'].get.responses['404']").exists());
    }

    @Test
    @DisplayName("Should serve the Swagger UI index page without being captured by the redirect route")
    void shouldServeSwaggerUiIndex() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should not answer the legacy /swagger-ui.html path with the redirect route's 404")
    void shouldNotReturnNotFoundForLegacySwaggerUiPath() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    org.junit.jupiter.api.Assertions.assertTrue(
                            status == 200 || status == 302,
                            "Expected 200 or 302 but was " + status);
                });
    }
}
