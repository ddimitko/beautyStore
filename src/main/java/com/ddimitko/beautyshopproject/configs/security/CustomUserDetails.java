package com.ddimitko.beautyshopproject.configs.security;

import com.ddimitko.beautyshopproject.entities.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // Ignore any unrecognized properties
public class CustomUserDetails implements UserDetails, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Getter
    private Long userId;
    @Getter
    private String firstName;
    @Getter
    private String lastName;

    private String username;
    @JsonIgnore
    private String password;
    private List<SerializableGrantedAuthority> authorities;

    public CustomUserDetails(User user) {
        this.userId = user.getId();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.username = user.getEmail();
        this.password = user.getPassword();
        // Get the role from the user
        this.authorities = List.of(new SerializableGrantedAuthority("ROLE_" + user.getRole().name()));

    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
