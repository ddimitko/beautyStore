package com.ddimitko.beautyshopproject.controllers;

import com.ddimitko.beautyshopproject.configs.security.CustomUserDetails;
import com.ddimitko.beautyshopproject.entities.Notification;
import com.ddimitko.beautyshopproject.services.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<Notification>> getUserNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long userId = userDetails.getUserId();
        List<Notification> notifications = notificationService.getNotifications(userId);

        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/read/{notificationId}")
    public ResponseEntity<Void> markAsRead(@PathVariable String notificationId) {
        notificationService.markNotificationAsRead(notificationId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all/")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal CustomUserDetails userDetails) {

        if(userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long userId = userDetails.getUserId();
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unread-count/{userId}")
    public long getUnreadNotificationCount(@PathVariable Long userId) {
        return notificationService.getUnreadCount(userId);
    }
}
