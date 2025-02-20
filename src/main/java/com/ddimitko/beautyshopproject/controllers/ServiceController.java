package com.ddimitko.beautyshopproject.controllers;

import com.ddimitko.beautyshopproject.Dto.responses.ServiceResponseDto;
import com.ddimitko.beautyshopproject.services.ServicesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/service")
public class ServiceController {

    private final ServicesService servicesService;

    public ServiceController(ServicesService servicesService) {
        this.servicesService = servicesService;
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<ServiceResponseDto>> getServicesForEmployee(@PathVariable long employeeId) {
        return ResponseEntity.ok(servicesService.getAllServicesForEmployee(employeeId));
    }

    @PostMapping("/employee/{employeeId}")
    public ResponseEntity<ServiceResponseDto> createServiceForEmployee(@PathVariable long employeeId, @RequestBody ServiceResponseDto serviceResponseDto) {
        ServiceResponseDto service = servicesService.addServiceToEmployee(serviceResponseDto, employeeId);
        return ResponseEntity.ok(service);
    }
}
