package com.ddimitko.beautyshopproject.configs.security;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.security.core.GrantedAuthority;

import java.io.Serial;
import java.io.Serializable;

public class SerializableGrantedAuthority implements GrantedAuthority, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String authority;

    @JsonCreator
    public SerializableGrantedAuthority(@JsonProperty("authority") String authority) {
        this.authority = authority;
    }

    @Override
    public String getAuthority() {
        return authority;
    }
}
