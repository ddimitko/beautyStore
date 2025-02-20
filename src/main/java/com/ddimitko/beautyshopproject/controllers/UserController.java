package com.ddimitko.beautyshopproject.controllers;

import com.ddimitko.beautyshopproject.Dto.responses.UserProfile;
import com.ddimitko.beautyshopproject.Dto.responses.UserResponseDto;
import com.ddimitko.beautyshopproject.Dto.responses.EmployeeResponseDto;
import com.ddimitko.beautyshopproject.configs.security.CustomUserDetails;
import com.ddimitko.beautyshopproject.entities.User;
import com.ddimitko.beautyshopproject.mappers.UserMapper;
import com.ddimitko.beautyshopproject.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @GetMapping("/cust")
    public List<UserResponseDto> getAllCustomers() {
        return userService.findAllCustomers();
    }

    @GetMapping("/cust/{id}")
    public UserResponseDto getCustomerById(@PathVariable int id) {
        return userMapper.mapUserToResponseDto(userService.getUserById(id));
    }

    @GetMapping("/empl")
    public List<EmployeeResponseDto> getAllEmployees() {
        return userService.findAllEmployees();
    }

    @GetMapping("/me")
    public ResponseEntity getCurrentUser(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        if(customUserDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        User user = userService.getUserById(customUserDetails.getUserId());
        // Extract role from authorities
        String role = customUserDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_GUEST"); // Default role if somehow empty

        return ResponseEntity.ok(new UserResponseDto(customUserDetails.getUserId(), customUserDetails.getFirstName(),
                customUserDetails.getLastName(),
                customUserDetails.getUsername(), user.getProfilePicture(), role));

    }

    @PutMapping("/profile/{userId}/upload")
    public ResponseEntity<String> uploadProfilePicture(@PathVariable Long userId, @RequestParam("file") MultipartFile file) {
        try {
            String fileUrl = userService.uploadProfilePicture(userId, file);
            return ResponseEntity.ok(fileUrl);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error uploading profile picture");
        }
    }

    @DeleteMapping("/profile/{userId}/delete")
    public ResponseEntity<String> deleteProfilePicture(@PathVariable Long userId) {
        User user = userService.getUserById(userId);
        if (user == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");

        if (user.getProfilePicture() == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No profile picture to delete");

        userService.deleteProfilePicture(user.getId(), user.getProfilePicture());

        user.setProfilePicture(null);

        return ResponseEntity.ok("Profile picture deleted successfully");
    }

    @GetMapping("/profile")
    public ResponseEntity getProfile(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        if(customUserDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }
        User user = userService.getUserById(customUserDetails.getUserId());

        return ResponseEntity.ok(new UserProfile(user.getFirstName(), user.getLastName(), user.getEmail(), user.getPhone()));
    }
}
