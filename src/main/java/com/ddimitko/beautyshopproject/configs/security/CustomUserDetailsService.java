package com.ddimitko.beautyshopproject.configs.security;

import com.ddimitko.beautyshopproject.entities.User;
import com.ddimitko.beautyshopproject.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("🔍 Loading user: " + username);  // <-- Debugging Log
        User user = userService.findUserByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String role = "ROLE_" + user.getRole().name();
        System.out.println("Loading user: " + user.getEmail() + " with role: " + role);

        // Map the role to authorities
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));

        return new CustomUserDetails(user);
    }
}
