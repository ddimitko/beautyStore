package com.ddimitko.beautyshopproject.configs.sockets;

import com.ddimitko.beautyshopproject.Dto.calendar.TimeSlotDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class AppointmentWebSocketConfig {

    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public AppointmentWebSocketConfig(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendUpdatedTimeSlots(Long employeeId, int serviceId, LocalDate date, List<TimeSlotDto> updatedSlots) {
        String topic = String.format("/topic/timeSlots/%d/%d/%s", employeeId, serviceId, date);
        messagingTemplate.convertAndSend(topic, updatedSlots);
    }
}
