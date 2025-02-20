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


@Service
public class AuthService {

    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final UserMapper userMapper;

    private final RedisTemplate<String, Object> redisTemplate;
    private final String REFRESH_TOKEN_KEY = "refresh_token";

    public AuthService(JwtService jwtService, AuthenticationManager authenticationManager, UserService userService, UserMapper userMapper, RedisTemplate<String, Object> redisTemplate) {
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.userMapper = userMapper;
        this.redisTemplate = redisTemplate;
    }

    public LoginResponse login(LoginRequest loginRequest) {
        System.out.println("Attempting login for: " + loginRequest.getEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );
        System.out.println("Authentication result: " + authentication.isAuthenticated());
        if (authentication.isAuthenticated()) {

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.findUserByEmail(userDetails.getUsername()).orElseThrow(()-> new RuntimeException("User not found."));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            System.out.println("Authentication successful for: " + loginRequest.getEmail());
            String token = jwtService.generateToken(loginRequest.getEmail());
            String refreshToken = jwtService.generateRefreshToken(loginRequest.getEmail());

            // Store refresh token in Redis
            redisTemplate.opsForHash().put(REFRESH_TOKEN_KEY, loginRequest.getEmail(), refreshToken);
            UserResponseDto userDto = userMapper.mapUserToResponseDto(user);
            System.out.println("🔍 Checking SecurityContextHolder: " + SecurityContextHolder.getContext().getAuthentication());
            return new LoginResponse(token, refreshToken, userDto);
        } else {
            System.out.println("Authentication failed for: " + loginRequest.getEmail());
            throw new UsernameNotFoundException("Invalid user request!");
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
        redisTemplate.opsForHash().delete(REFRESH_TOKEN_KEY, username);
        SecurityContextHolder.clearContext();
    }
}
