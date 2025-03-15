package com.ddimitko.beautyshopproject.controllers;

import com.ddimitko.beautyshopproject.Dto.requests.LoginRequest;
import com.ddimitko.beautyshopproject.Dto.requests.SignupRequest;
import com.ddimitko.beautyshopproject.Dto.responses.LoginResponse;
import com.ddimitko.beautyshopproject.services.AuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest signupRequest) {
        authService.signup(signupRequest);
        return login(new LoginRequest(signupRequest.getEmail(), signupRequest.getPassword()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        // Delegate to AuthService
        LoginResponse response = authService.login(loginRequest);

        // Send updated tokens as HTTP-only cookies
        ResponseCookie accessCookie = createHttpOnlyCookie("accessToken", response.getAccessToken(), 900);
        ResponseCookie refreshCookie = createHttpOnlyCookie("refreshToken", response.getRefreshToken(), 2592000); // 7 days

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(response.getUser()); // Return updated user details to frontend
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@CookieValue(name = "refreshToken", required = false) String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.ok("No action needed, already logged out.");
        }

        authService.logout(refreshToken);

        ResponseCookie accessCookie = ResponseCookie.from("accessToken", "").path("/")
                .maxAge(0).build();
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", "").path("/")
                .maxAge(0).build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body("Logged out successfully.");
    }

    // Utility method for creating HTTP-only cookies
    private ResponseCookie createHttpOnlyCookie(String name, String value, long maxAge) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(false) // Change to true if HTTPS
                .path("/")
                .sameSite("Strict")
                .maxAge(maxAge)
                .build();
    }
}
