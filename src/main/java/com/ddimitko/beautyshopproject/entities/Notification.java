package com.ddimitko.beautyshopproject.entities;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class Notification {

    private String id;
    private String message;
    private Long recipientId;

    private LocalDateTime createdAt;
    private boolean read = false;

    public Notification(String message, Long recipientId) {
        this.id = UUID.randomUUID().toString();
        this.message = message;
        this.recipientId = recipientId;
        this.createdAt = LocalDateTime.now();
    }

    public void markAsRead() {
        this.read = true;
    }
}
