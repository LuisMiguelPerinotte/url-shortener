package com.luismiguel.java.url_shortener.application.service;

import com.luismiguel.java.url_shortener.TestcontainersConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.IntFunction;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class ShortURLServiceConcurrencyTest {

    @Autowired
    private ShortURLService service;

    @Autowired
    private PlatformTransactionManager txManager;

    @Autowired
    private JdbcTemplate jdbc;

    private ExecutorService executor;

    @BeforeEach
    void setUp() {
        executor = Executors.newFixedThreadPool(20);
    }

    @AfterEach
    void tearDown() {
        executor.shutdownNow();
    }

    @Test
    @DisplayName("Should Reuse Same Row When Second Transaction Waits For First With Same Url")
    void sameUrlSecondTransactionWaitsAndReusesRow() throws Exception {
        String url = newUrl();
        CountDownLatch t1Inserted = new CountDownLatch(1);
        CountDownLatch releaseT1 = new CountDownLatch(1);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        ExecutorService pair = Executors.newFixedThreadPool(2);

        try {
            Future<String> first = pair.submit(() -> tx.execute(status -> {
                String code = service.shortenURL(url).shortCode();
                t1Inserted.countDown();
                awaitQuietly(releaseT1);
                return code;
            }));

            assertTrue(t1Inserted.await(10, SECONDS), "first transaction never inserted the row");

            Future<String> second = pair.submit(() -> service.shortenURL(url).shortCode());
            awaitSecondTransactionBlocked();
            releaseT1.countDown();

            String firstCode = first.get(30, SECONDS);
            String secondCode = second.get(30, SECONDS);

            assertEquals(firstCode, secondCode);
            assertEquals(1L, count(url));
        } finally {
            releaseT1.countDown();
            pair.shutdownNow();
        }
    }

    @Test
    @DisplayName("Should Produce Single Row When Twenty Concurrent Requests Use Same Url")
    void twentyConcurrentRequestsSameUrlProduceSingleRow() throws Exception {
        String url = newUrl();

        List<String> codes = runConcurrently(20, i -> () -> service.shortenURL(url).shortCode());

        assertEquals(1, codes.stream().distinct().count());
        assertEquals(1L, count(url));
    }

    @Test
    @DisplayName("Should Produce Distinct Codes When Concurrent Requests Use Different Urls")
    void differentUrlsConcurrentlyProduceDistinctCodes() throws Exception {
        List<String> urls = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            urls.add(newUrl());
        }

        List<String> codes = runConcurrently(20, i -> () -> service.shortenURL(urls.get(i)).shortCode());

        assertEquals(20, codes.stream().distinct().count());
        for (String url : urls) {
            assertEquals(1L, count(url));
        }
    }

    // helpers

    private String newUrl() {
        return "https://example.com/" + UUID.randomUUID();
    }

    private Long count(String url) {
        return jdbc.queryForObject("SELECT count(*) FROM short_url WHERE original_url = ?", Long.class, url);
    }

    private void awaitSecondTransactionBlocked() throws InterruptedException {
        long deadline = System.nanoTime() + SECONDS.toNanos(10);
        while (blockedLockCount() == 0) {
            if (System.nanoTime() > deadline) {
                fail("second transaction never blocked waiting for the lock");
            }
            Thread.sleep(50);
        }
    }

    private Long blockedLockCount() {
        return jdbc.queryForObject("SELECT count(*) FROM pg_locks WHERE NOT granted", Long.class);
    }

    private static void awaitQuietly(CountDownLatch latch) {
        try {
            if (!latch.await(20, SECONDS)) {
                throw new IllegalStateException("timed out waiting for latch");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    private List<String> runConcurrently(int threads, IntFunction<Callable<String>> taskFactory)
            throws Exception {
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<String>> futures = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            Callable<String> task = taskFactory.apply(i);
            futures.add(executor.submit(() -> {
                ready.countDown();
                start.await();
                return task.call();
            }));
        }

        assertTrue(ready.await(10, SECONDS), "workers did not become ready");
        start.countDown();

        List<String> results = new ArrayList<>();
        for (Future<String> future : futures) {
            results.add(future.get(30, SECONDS));
        }
        return results;
    }
}
