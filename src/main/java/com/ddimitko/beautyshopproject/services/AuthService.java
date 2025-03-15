package com.ddimitko.beautyshopproject.services;

import com.ddimitko.beautyshopproject.Dto.requests.LoginRequest;
import com.ddimitko.beautyshopproject.Dto.requests.SignupRequest;
import com.ddimitko.beautyshopproject.Dto.responses.LoginResponse;
import com.ddimitko.beautyshopproject.Dto.responses.UserResponseDto;
import com.ddimitko.beautyshopproject.configs.jwt.JwtService;
import com.ddimitko.beautyshopproject.entities.User;
import com.ddimitko.beautyshopproject.mappers.UserMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Duration;


@Service
public class AuthService {

    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final UserMapper userMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";

    public AuthService(JwtService jwtService, AuthenticationManager authenticationManager, UserService userService,
                       UserMapper userMapper, RedisTemplate<String, Object> redisTemplate) {
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.userMapper = userMapper;
        this.redisTemplate = redisTemplate;
    }

    public LoginResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        if (authentication.isAuthenticated()) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.findUserByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found."));

            String accessToken = jwtService.generateToken(user.getEmail());
            String refreshToken = jwtService.generateRefreshToken(user.getEmail());

            redisTemplate.opsForValue().set(REFRESH_TOKEN_PREFIX + user.getEmail(), refreshToken, Duration.ofDays(30));

            return new LoginResponse(accessToken, refreshToken, userMapper.mapUserToResponseDto(user));
        } else {
            throw new UsernameNotFoundException("Invalid credentials!");
        }
    }

    public void signup(SignupRequest signupRequest) {
        if(signupRequest.getFirstName().isEmpty() ||
                signupRequest.getLastName().isEmpty() ||
                signupRequest.getEmail().isEmpty() ||
                signupRequest.getPassword().isEmpty()){
            throw new RuntimeException("Fields cannot be empty");
        }
        userService.saveUser(signupRequest);
    }

    public void logout(String refreshToken) {
        String username = jwtService.extractUsername(refreshToken);
        if (username != null) {
            redisTemplate.delete(REFRESH_TOKEN_PREFIX + username);
        }
        SecurityContextHolder.clearContext();
    }
}
