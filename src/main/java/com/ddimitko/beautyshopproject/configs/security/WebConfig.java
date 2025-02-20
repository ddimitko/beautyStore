package com.ddimitko.beautyshopproject.configs.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Apply CORS to all endpoints starting with /api
                .allowedOrigins("http://localhost:3000") // Allow requests from your React app (adjust the port if needed)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH") // Allow the necessary HTTP methods
                .allowedHeaders("*") // Allow all headers
                .allowCredentials(true);
    }
}
