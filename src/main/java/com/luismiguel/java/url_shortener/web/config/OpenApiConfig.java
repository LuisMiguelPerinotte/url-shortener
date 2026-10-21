package com.luismiguel.java.url_shortener.web.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI urlShortenerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("URL Shortener API")
                        .description("REST API to shorten URLs, redirect short codes and read click statistics.")
                        .version("1.1.0")
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/license/mit")));
    }
}
