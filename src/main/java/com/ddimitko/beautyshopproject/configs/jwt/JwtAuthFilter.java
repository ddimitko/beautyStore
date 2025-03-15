package com.ddimitko.beautyshopproject.configs.jwt;

import com.ddimitko.beautyshopproject.configs.security.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
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
    private final RedisTemplate<String, String> redisTemplate;
    private final CustomUserDetailsService userDetailsService;
    private static final String REFRESH_TOKEN_KEY_PREFIX = "refresh_token:";

    public JwtAuthFilter(JwtService jwtService, RedisTemplate<String, String> redisTemplate, CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.redisTemplate = redisTemplate;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String accessToken = extractAccessTokenFromCookies(request);
        String refreshToken = extractRefreshTokenFromCookies(request);
        Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();

        if (existingAuth != null && existingAuth.isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }

        if (accessToken != null && jwtService.isTokenValid(accessToken)) {
            authenticateUser(accessToken, request);
        } else if (refreshToken != null && jwtService.isTokenValid(refreshToken)) {
            refreshTokensAndAuthenticate(refreshToken, request, response);
        }

        filterChain.doFilter(request, response);
    }

    private void refreshTokensAndAuthenticate(String refreshToken, HttpServletRequest request, HttpServletResponse response) {
        String username = jwtService.extractUsername(refreshToken);
        String storedToken = redisTemplate.opsForValue().get(REFRESH_TOKEN_KEY_PREFIX + username);

        if (storedToken == null || !storedToken.equals(refreshToken)) return;

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        String newAccessToken = jwtService.generateToken(username);
        String newRefreshToken = jwtService.generateRefreshToken(username);

        redisTemplate.opsForValue().set(REFRESH_TOKEN_KEY_PREFIX + username, newRefreshToken, Duration.ofDays(30));
        response.addHeader(HttpHeaders.SET_COOKIE, createHttpOnlyCookie("accessToken", newAccessToken, 900).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, createHttpOnlyCookie("refreshToken", newRefreshToken, 2592000).toString());
        authenticate(userDetails, request);
    }

    private void authenticateUser(String token, HttpServletRequest request) {
        String username = jwtService.extractUsername(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        authenticate(userDetails, request);
    }

    private void authenticate(UserDetails userDetails, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    private ResponseCookie createHttpOnlyCookie(String name, String value, long maxAge) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Strict")
                .maxAge(maxAge)
                .build();
    }

    private String extractAccessTokenFromCookies(HttpServletRequest request) {
        return extractCookie(request, "accessToken");
    }

    private String extractRefreshTokenFromCookies(HttpServletRequest request) {
        return extractCookie(request, "refreshToken");
    }

    private String extractCookie(HttpServletRequest request, String cookieName) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals(cookieName)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
