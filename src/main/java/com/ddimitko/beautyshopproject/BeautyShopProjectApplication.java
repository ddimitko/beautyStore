package com.ddimitko.beautyshopproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class BeautyShopProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(BeautyShopProjectApplication.class, args);
    }

}
