package com.ddimitko.beautyshopproject.services;

import com.ddimitko.beautyshopproject.entities.Notification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

@Service
public class NotificationService {

    private final List<Notification> notifications;

    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(List<Notification> notifications, SimpMessagingTemplate messagingTemplate) {
        this.notifications = notifications;
        this.messagingTemplate = messagingTemplate;
    }

    public void saveNotification(Notification notification) {
        notifications.add(notification);
    }

    public List<Notification> getNotifications(Long userId) {
        return notifications.stream().filter(n -> n.getRecipientId()
                .equals(userId))
                .sorted(Comparator.comparing(Notification::getCreatedAt).reversed()).toList();
    }

    public void sendAppointmentNotification(Long employeeId, String customerName, LocalDate appointmentDate, LocalTime appointmentTime) {
        String message = "Appointment created by " + customerName + " for " + appointmentDate + " " + appointmentTime;

        Notification employeeNotification = new Notification(message, employeeId);

        saveNotification(employeeNotification);

        messagingTemplate.convertAndSend("/topic/notifications/" + employeeId, employeeNotification);

    }

    public void sendReminderNotification(Long customerId, Long employeeId, String customerName, LocalTime appointmentTime) {

        if(customerId != null) {
            String message = "REMINDER: You have an appointment in 1 hour at " + appointmentTime;

            Notification customerNotification = new Notification(message, customerId);
            Notification employeeNotification = new Notification(message, employeeId);

            employeeNotification.setMessage(message + " by " + customerName);
            employeeNotification.setRecipientId(employeeId);

            saveNotification(customerNotification);
            saveNotification(employeeNotification);

            messagingTemplate.convertAndSend("/topic/notifications/" + customerId, customerNotification);
            messagingTemplate.convertAndSend("/topic/notifications/" + employeeId, employeeNotification);
        }
    }

    public void markNotificationAsRead(String notificationId) {
        notifications.stream()
                .filter(n -> n.getId().equals(notificationId))
                .findFirst()
                .ifPresent(Notification::markAsRead);
    }

    public void markAllAsRead(Long userId) {
        notifications.forEach(n -> {
            if (n.getRecipientId().equals(userId)) {
                n.setRead(true);
            }
        });
    }

    public long getUnreadCount(Long userId) {
        return notifications.stream()
                .filter(n -> n.getRecipientId().equals(userId) && !n.isRead())
                .count();
    }
}
