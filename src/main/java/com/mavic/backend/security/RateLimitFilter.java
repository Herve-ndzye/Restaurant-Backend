package com.mavic.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mavic.backend.exception.ErrorResponse;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {
    
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // Rate limit: 5 requests per minute for auth endpoints
    private static final int AUTH_REQUESTS_PER_MINUTE = 5;
    
    // Rate limit: 20 requests per minute for other endpoints
    private static final int GENERAL_REQUESTS_PER_MINUTE = 20;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        String path = request.getRequestURI();
        
        // Apply rate limiting only to specific endpoints
        if (shouldRateLimit(path)) {
            String key = getClientKey(request, path);
            Bucket bucket = resolveBucket(key, isAuthEndpoint(path));
            
            if (bucket.tryConsume(1)) {
                filterChain.doFilter(request, response);
            } else {
                log.warn("Rate limit exceeded for key: {}", key);
                sendRateLimitError(response);
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }

    private boolean shouldRateLimit(String path) {
        return path.startsWith("/api/auth/") || 
               path.startsWith("/api/orders") || 
               path.startsWith("/api/customer");
    }

    private boolean isAuthEndpoint(String path) {
        return path.startsWith("/api/auth/");
    }

    private String getClientKey(HttpServletRequest request, String path) {
        // Use IP address + endpoint as key
        String clientIp = getClientIP(request);
        return clientIp + ":" + path;
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    private Bucket resolveBucket(String key, boolean isAuthEndpoint) {
        return cache.computeIfAbsent(key, k -> createNewBucket(isAuthEndpoint));
    }

    private Bucket createNewBucket(boolean isAuthEndpoint) {
        int requestsPerMinute = isAuthEndpoint ? AUTH_REQUESTS_PER_MINUTE : GENERAL_REQUESTS_PER_MINUTE;
        
        Bandwidth limit = Bandwidth.classic(
                requestsPerMinute, 
                Refill.intervally(requestsPerMinute, Duration.ofMinutes(1))
        );
        
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private void sendRateLimitError(HttpServletResponse response) throws IOException {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.TOO_MANY_REQUESTS.value())
                .error("Rate Limit Exceeded")
                .message("Too many requests. Please try again later.")
                .build();
        
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
