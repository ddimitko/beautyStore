package com.ddimitko.beautyshopproject.configs.jwt;

import com.ddimitko.beautyshopproject.configs.security.CustomUserDetails;
import com.ddimitko.beautyshopproject.configs.security.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    private final RedisTemplate<String, Object> redisTemplate;
    private final String REFRESH_TOKEN_KEY = "refresh_token";

    private final CustomUserDetailsService userDetailsService;

    public JwtAuthFilter(JwtService jwtService, RedisTemplate<String, Object> redisTemplate, CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.redisTemplate = redisTemplate;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestPath = request.getRequestURI();
        System.out.println("🛑 JWT Filter START for: " + requestPath);

        // Skip authentication for public endpoints
        if (requestPath.startsWith("/auth/signup") || requestPath.startsWith("/auth/login")) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = extractAccessTokenFromCookies(request);
        String refreshToken = extractRefreshTokenFromCookies(request);
        Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();

        if (existingAuth != null && existingAuth.isAuthenticated() && !(existingAuth instanceof AnonymousAuthenticationToken)) {
            System.out.println("🔍 User already authenticated: " + existingAuth.getName());

            // 🔹 Still check if the token is valid to prevent using an expired one
            if (accessToken != null && jwtService.isTokenValid(accessToken)) {
                System.out.println("✅ Access token is still valid, skipping re-authentication.");
                filterChain.doFilter(request, response);
                return;
            } else if (refreshToken != null && jwtService.isTokenValid(refreshToken)) {
                System.out.println("🔄 Access token expired, refreshing...");
                refreshTokensAndAuthenticate(refreshToken, request, response);
                filterChain.doFilter(request, response);
                return;
            }

            // If tokens are invalid, clear the authentication
            SecurityContextHolder.clearContext();
        }

        if (accessToken != null && jwtService.isTokenValid(accessToken)) {
            authenticateUser(accessToken, request);
        } else if (refreshToken != null && jwtService.isTokenValid(refreshToken)) {
            refreshTokensAndAuthenticate(refreshToken, request, response);
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateUser(String token, HttpServletRequest request) {
        String username = jwtService.extractUsername(token);
        if (username == null) return;

        // 🔹 Try to fetch UserDetails from Redis before hitting DB
        CustomUserDetails userDetails = (CustomUserDetails) redisTemplate.opsForValue().get("USER_CACHE:" + username);

        if (userDetails == null) {
            System.out.println("⚠️ UserDetails not found in Redis. Loading from DB...");
            userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(username);

            // ✅ Cache UserDetails in Redis
            redisTemplate.opsForValue().set("USER_CACHE:" + username, userDetails, Duration.ofMinutes(30));
        } else {
            System.out.println("✅ UserDetails loaded from Redis cache for: " + username);
        }

        // ✅ Validate and authenticate user
        if (jwtService.validateToken(token, userDetails)) {
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);

            System.out.println("✅ JWT Authentication set for: " + username);
        }
    }

    private void refreshTokensAndAuthenticate(String refreshToken, HttpServletRequest request, HttpServletResponse response) {
        if (!jwtService.isTokenValid(refreshToken)) return;

        String username = jwtService.extractUsername(refreshToken);

        // 🔹 Check if the refresh token is valid from Redis
        Object storedToken = redisTemplate.opsForHash().get(REFRESH_TOKEN_KEY, username);
        if (storedToken == null || !refreshToken.equals(storedToken.toString())) return;

        // 🔹 Try to fetch UserDetails from Redis before hitting DB
        CustomUserDetails userDetails = (CustomUserDetails) redisTemplate.opsForValue().get("USER_CACHE:" + username);

        if (userDetails == null) {
            userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(username);
            redisTemplate.opsForValue().set("USER_CACHE:" + username, userDetails, Duration.ofMinutes(30)); // Cache for 30 min
        }

        // 🔹 Generate new tokens
        String newAccessToken = jwtService.generateToken(username);
        String newRefreshToken = jwtService.generateRefreshToken(username);

        redisTemplate.opsForHash().put(REFRESH_TOKEN_KEY, username, newRefreshToken); // Update refresh token in Redis

        ResponseCookie accessCookie = createHttpOnlyCookie("accessToken", newAccessToken, 60 * 2);
        ResponseCookie refreshCookie = createHttpOnlyCookie("refreshToken", newRefreshToken, 7 * 24 * 60 * 60);

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        // 🔹 Authenticate user without hitting DB
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);

        System.out.println("✅ Tokens refreshed & User authenticated: " + username);
    }

    /**
     * Extracts JWT access token from cookies.
     */
    private String extractAccessTokenFromCookies(HttpServletRequest request) {
        return extractCookie(request, "accessToken");
    }

    /**
     * Extracts JWT refresh token from cookies.
     */
    private String extractRefreshTokenFromCookies(HttpServletRequest request) {
        return extractCookie(request, "refreshToken");
    }

    /**
     * Extracts a cookie value from the request.
     */
    private String extractCookie(HttpServletRequest request, String cookieName) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * Creates an HTTP-only secure cookie.
     */
    private ResponseCookie createHttpOnlyCookie(String name, String value, long maxAge) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(false) // ✅ Set to true for HTTPS
                .path("/")
                .sameSite("Strict") // ✅ Required for cross-origin cookies
                .maxAge(maxAge)
                .build();
    }

}
