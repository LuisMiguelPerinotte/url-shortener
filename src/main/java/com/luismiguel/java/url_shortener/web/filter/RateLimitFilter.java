package com.luismiguel.java.url_shortener.web.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Per-IP rate limit with two buckets: writes (POST /api/v1/short-url) and everything else (reads).
 * Shortcut: in-memory per instance; move to a shared store (e.g. Redis) if the app is scaled out.
 * Shortcut: proxyHops = number of trusted proxies in front of the app (0 = use getRemoteAddr()); the client IP is
 * the n-th X-Forwarded-For entry from the right, the last entry being 1. Entries further left are client-controlled
 * and ignored.
 */
@Slf4j
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String SHORTEN_PATH = "/api/v1/short-url";
    private static final String FORWARDED_FOR = "X-Forwarded-For";
    private static final Duration WINDOW = Duration.ofMinutes(1);

    private final Cache<String, Bucket> writeBuckets;
    private final Cache<String, Bucket> readBuckets;
    private final ObjectMapper objectMapper;
    private final int writePerMinute;
    private final int readPerMinute;
    private final int proxyHops;

    /**
     * @param proxyHops number of trusted proxies in front of the app (0 = use getRemoteAddr()). The client IP is the
     *                  n-th X-Forwarded-For entry from the right (1 = last entry).
     */
    public RateLimitFilter(ObjectMapper objectMapper,
                           @Value("${app.rate-limit.write-per-minute:10}") int writePerMinute,
                           @Value("${app.rate-limit.read-per-minute:120}") int readPerMinute,
                           @Value("${app.rate-limit.proxy-hops:0}") int proxyHops) {
        this.objectMapper = objectMapper;
        this.writePerMinute = writePerMinute;
        this.readPerMinute = readPerMinute;
        this.proxyHops = proxyHops;
        this.writeBuckets = newCache();
        this.readBuckets = newCache();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        boolean write = HttpMethod.POST.matches(request.getMethod()) && SHORTEN_PATH.equals(request.getRequestURI());
        Cache<String, Bucket> cache = write ? writeBuckets : readBuckets;
        int limit = write ? writePerMinute : readPerMinute;

        String key = clientIp(request);
        if (log.isDebugEnabled()) {
            log.debug("Rate limit key={} remoteAddr={} X-Forwarded-For={} Forwarded={} True-Client-IP={} CF-Connecting-IP={} X-Real-IP={}",
                    key, request.getRemoteAddr(), String.join(" | ", forwardedForLines(request)),
                    request.getHeader("Forwarded"), request.getHeader("True-Client-IP"),
                    request.getHeader("CF-Connecting-IP"), request.getHeader("X-Real-IP"));
        }

        Bucket bucket = cache.get(key, k -> newBucket(limit));
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            chain.doFilter(request, response);
            return;
        }

        writeTooManyRequests(request, response, probe.getNanosToWaitForRefill());
    }

    private String clientIp(HttpServletRequest request) {
        if (proxyHops <= 0) {
            return request.getRemoteAddr();
        }
        List<String> entries = new ArrayList<>();
        for (String line : forwardedForLines(request)) {
            for (String part : line.split(",")) {
                String entry = part.trim();
                if (!entry.isEmpty()) {
                    entries.add(entry);
                }
            }
        }
        int index = entries.size() - proxyHops;
        return index < 0 ? request.getRemoteAddr() : entries.get(index);
    }

    private static List<String> forwardedForLines(HttpServletRequest request) {
        return Collections.list(request.getHeaders(FORWARDED_FOR));
    }

    private void writeTooManyRequests(HttpServletRequest request, HttpServletResponse response, long nanosToWait)
            throws IOException {
        long retryAfterSeconds = Math.max(1L, (nanosToWait + 999_999_999L) / 1_000_000_000L);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
        body.put("error", HttpStatus.TOO_MANY_REQUESTS.name());
        body.put("message", "Too many requests");
        body.put("path", request.getRequestURI());

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(retryAfterSeconds));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), body);
    }

    private Bucket newBucket(int limit) {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(limit)
                        .refillGreedy(limit, WINDOW)
                        .build())
                .build();
    }

    private static Cache<String, Bucket> newCache() {
        return Caffeine.newBuilder()
                .maximumSize(100_000)
                .expireAfterAccess(Duration.ofMinutes(10))
                .build();
    }
}
