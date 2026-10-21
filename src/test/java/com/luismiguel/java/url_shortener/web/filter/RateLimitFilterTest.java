package com.luismiguel.java.url_shortener.web.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RateLimitFilterTest {

    private static final String SHORTEN_PATH = "/api/v1/short-url";

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = mockMvc(0);
    }

    // Fresh filter per call so the buckets start empty: 2 writes and 3 reads per minute.
    private static MockMvc mockMvc(int proxyHops) {
        return MockMvcBuilders.standaloneSetup(new FakeController())
                .addFilters(new RateLimitFilter(new ObjectMapper(), 2, 3, proxyHops))
                .build();
    }

    @Test
    @DisplayName("Should Use Last X-Forwarded-For Entry When Proxy Hops Is One")
    void shouldUseLastForwardedForEntryWhenProxyHopsIsOne() throws Exception {
        MockMvc proxied = mockMvc(1);
        // Left entries are client-forged and change every time; the real client is the last one.
        proxied.perform(post(SHORTEN_PATH).header("X-Forwarded-For", "203.0.113.1, 198.51.100.7"))
                .andExpect(status().isOk());
        proxied.perform(post(SHORTEN_PATH).header("X-Forwarded-For", "203.0.113.2, 198.51.100.7"))
                .andExpect(status().isOk());
        proxied.perform(post(SHORTEN_PATH).header("X-Forwarded-For", "203.0.113.3, 198.51.100.7"))
                .andExpect(status().isTooManyRequests());

        proxied.perform(post(SHORTEN_PATH).header("X-Forwarded-For", "203.0.113.4, 198.51.100.8"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should Ignore X-Forwarded-For When Proxy Hops Is Zero")
    void shouldIgnoreForwardedForWhenProxyHopsIsZero() throws Exception {
        mockMvc.perform(post(SHORTEN_PATH).header("X-Forwarded-For", "203.0.113.1")).andExpect(status().isOk());
        mockMvc.perform(post(SHORTEN_PATH).header("X-Forwarded-For", "203.0.113.2")).andExpect(status().isOk());
        mockMvc.perform(post(SHORTEN_PATH).header("X-Forwarded-For", "203.0.113.3"))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    @DisplayName("Should Fall Back To Remote Address When Forwarded Chain Is Too Short")
    void shouldFallBackToRemoteAddressWhenForwardedChainIsTooShort() throws Exception {
        // Two proxies but only one entry: the chain cannot hold the client, so getRemoteAddr() is used.
        MockMvc twoHops = mockMvc(2);
        twoHops.perform(post(SHORTEN_PATH).header("X-Forwarded-For", "203.0.113.1")
                        .with(remoteAddr("10.0.0.5")))
                .andExpect(status().isOk());
        twoHops.perform(post(SHORTEN_PATH).header("X-Forwarded-For", "203.0.113.2")
                        .with(remoteAddr("10.0.0.5")))
                .andExpect(status().isOk());
        twoHops.perform(post(SHORTEN_PATH).header("X-Forwarded-For", "203.0.113.3")
                        .with(remoteAddr("10.0.0.5")))
                .andExpect(status().isTooManyRequests());

        // No header at all with one hop: also falls back to getRemoteAddr().
        MockMvc noHeader = mockMvc(1);
        noHeader.perform(post(SHORTEN_PATH).with(remoteAddr("10.0.0.6"))).andExpect(status().isOk());
        noHeader.perform(post(SHORTEN_PATH).with(remoteAddr("10.0.0.6"))).andExpect(status().isOk());
        noHeader.perform(post(SHORTEN_PATH).with(remoteAddr("10.0.0.6")))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    @DisplayName("Should Key On Client Entry Not Remote Address When Cloudflare Edge IP Changes")
    void shouldKeyOnClientEntryWhenCloudflareEdgeIpChanges() throws Exception {
        // Logged real case: the edge IP (remoteAddr) changes between requests, the header is the same.
        MockMvc render = mockMvc(1);
        String forwarded = "203.0.113.1,160.19.174.54";
        render.perform(post(SHORTEN_PATH).header("X-Forwarded-For", forwarded)
                        .with(remoteAddr("172.71.195.122")))
                .andExpect(status().isOk());
        render.perform(post(SHORTEN_PATH).header("X-Forwarded-For", forwarded)
                        .with(remoteAddr("104.23.211.53")))
                .andExpect(status().isOk());
        render.perform(post(SHORTEN_PATH).header("X-Forwarded-For", forwarded)
                        .with(remoteAddr("172.71.195.122")))
                .andExpect(status().isTooManyRequests());

        // A different real client behind the same edge gets its own bucket.
        render.perform(post(SHORTEN_PATH).header("X-Forwarded-For", "203.0.113.1,160.19.174.99")
                        .with(remoteAddr("104.23.211.53")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should Return 429 With Retry-After And Error Body When Write Limit Is Exceeded")
    void shouldReturn429WithRetryAfterWhenWriteLimitIsExceeded() throws Exception {
        shorten(); // 1
        shorten(); // 2

        MvcResult result = mockMvc.perform(post(SHORTEN_PATH))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.error").value("TOO_MANY_REQUESTS"))
                .andExpect(jsonPath("$.message").value("Too many requests"))
                .andExpect(jsonPath("$.path").value(SHORTEN_PATH))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andReturn();

        String retryAfter = result.getResponse().getHeader("Retry-After");
        assertTrue(retryAfter != null && Integer.parseInt(retryAfter) >= 1,
                "Retry-After must be an integer >= 1, was " + retryAfter);
    }

    @Test
    @DisplayName("Should Return 200 For Another IP While The First IP Is Blocked")
    void shouldReturn200ForAnotherIpWhenFirstIpIsBlocked() throws Exception {
        shorten();
        shorten();
        mockMvc.perform(post(SHORTEN_PATH)).andExpect(status().isTooManyRequests());

        mockMvc.perform(post(SHORTEN_PATH).with(remoteAddr("10.0.0.2")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should Keep Read Bucket Independent When Write Bucket Is Exhausted")
    void shouldKeepReadBucketIndependentWhenWriteBucketIsExhausted() throws Exception {
        shorten();
        shorten();
        mockMvc.perform(post(SHORTEN_PATH)).andExpect(status().isTooManyRequests());

        for (int i = 0; i < 3; i++) {
            mockMvc.perform(get("/abc123")).andExpect(status().isOk());
        }
        mockMvc.perform(get("/abc123")).andExpect(status().isTooManyRequests());
    }

    @Test
    @DisplayName("Should Never Limit Actuator Health Endpoint")
    void shouldNeverLimitActuatorHealthEndpoint() throws Exception {
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(get("/actuator/health")).andExpect(status().isOk());
        }
    }

    private void shorten() throws Exception {
        mockMvc.perform(post(SHORTEN_PATH).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    private static RequestPostProcessor remoteAddr(String address) {
        return request -> {
            request.setRemoteAddr(address);
            return request;
        };
    }

    @RestController
    static class FakeController {

        @PostMapping(SHORTEN_PATH)
        String shorten() {
            return "ok";
        }

        @GetMapping("/{code}")
        String redirect(@PathVariable String code) {
            return "ok";
        }

        @GetMapping("/actuator/health")
        String health() {
            return "UP";
        }
    }
}
