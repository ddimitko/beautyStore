package com.ddimitko.beautyshopproject.schedulers;

import com.ddimitko.beautyshopproject.repositories.AppointmentRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
public class AppointmentStatusScheduler {

    private static final Logger LOGGER = Logger.getLogger( AppointmentStatusScheduler.class.getName() );

    private final AppointmentRepository appointmentRepository;

    public AppointmentStatusScheduler(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    @Scheduled(fixedRate = 10 * 60 * 1000) // Every 10 minutes
    public void updateAppointments() {
        int updated = appointmentRepository.markPastAppointmentsAsCompleted();
        LOGGER.info("Updated " + updated + " appointments to Completed.");
    }
}
