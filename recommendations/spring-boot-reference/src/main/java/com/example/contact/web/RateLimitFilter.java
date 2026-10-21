package com.example.contact.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Addresses F-04: sliding-window limit of 5 POSTs per client IP per 10 minutes.
 * In-memory, so it is per instance. For several instances use Redis (e.g. Bucket4j) or a gateway/WAF.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS = 5;
    private static final Duration WINDOW = Duration.ofMinutes(10);

    private final ConcurrentHashMap<String, Deque<Instant>> hits = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !(HttpMethod.POST.matches(request.getMethod()) && "/api/contact".equals(request.getRequestURI()));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        // getRemoteAddr() is the real client IP only if the proxy setup is trusted (see application.yml).
        // Never read X-Forwarded-For directly: clients can forge it.
        String key = request.getRemoteAddr();
        Instant now = Instant.now();
        Deque<Instant> window = hits.computeIfAbsent(key, k -> new ArrayDeque<>());

        boolean allowed;
        long retryAfterSeconds = 0;
        synchronized (window) {
            Instant cutoff = now.minus(WINDOW);
            while (!window.isEmpty() && window.peekFirst().isBefore(cutoff)) {
                window.pollFirst();
            }
            if (window.size() < MAX_REQUESTS) {
                window.addLast(now);
                allowed = true;
            } else {
                allowed = false;
                retryAfterSeconds = Math.max(1, Duration.between(now, window.peekFirst().plus(WINDOW)).toSeconds());
            }
        }

        if (allowed) {
            chain.doFilter(request, response);
            return;
        }
        response.setStatus(429);
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        response.setContentType("application/problem+json");
        response.getWriter().write(
                "{\"type\":\"about:blank\",\"title\":\"Too Many Requests\",\"status\":429,"
                + "\"detail\":\"Please try again later.\"}");
    }

    /** Prevents unbounded memory growth from many distinct IPs. */
    @Scheduled(fixedRate = 600_000)
    void evictIdleClients() {
        Instant cutoff = Instant.now().minus(WINDOW);
        hits.entrySet().removeIf(e -> {
            synchronized (e.getValue()) {
                return e.getValue().isEmpty() || e.getValue().peekLast().isBefore(cutoff);
            }
        });
    }
}
