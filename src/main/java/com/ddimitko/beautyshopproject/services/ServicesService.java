package com.ddimitko.beautyshopproject.services;

import com.ddimitko.beautyshopproject.Dto.responses.ServiceResponseDto;
import com.ddimitko.beautyshopproject.entities.Employee;
import com.ddimitko.beautyshopproject.repositories.EmployeeRepository;
import com.ddimitko.beautyshopproject.repositories.ServiceRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ServicesService {

    private final ServiceRepository serviceRepository;
    private final EmployeeRepository employeeRepository;

    public ServicesService(ServiceRepository serviceRepository, EmployeeRepository employeeRepository) {
        this.serviceRepository = serviceRepository;
        this.employeeRepository = employeeRepository;
    }

    private ServiceResponseDto mapToDto(com.ddimitko.beautyshopproject.entities.Service service) {
        ServiceResponseDto serviceResponseDto = new ServiceResponseDto();
        serviceResponseDto.setName(service.getName());
        serviceResponseDto.setDescription(service.getDescription());
        serviceResponseDto.setPrice(service.getPrice());
        serviceResponseDto.setDurationInMinutes(service.getDurationInMinutes());
        return serviceResponseDto;
    }

    public ServiceResponseDto addServiceToEmployee(ServiceResponseDto serviceResponseDto, long employeeId) {
        Employee employee = employeeRepository.findById(employeeId).orElseThrow(() -> new RuntimeException("Employee not found"));

        com.ddimitko.beautyshopproject.entities.Service service = new com.ddimitko.beautyshopproject.entities.Service();
        service.setName(serviceResponseDto.getName());
        service.setDescription(serviceResponseDto.getDescription());
        service.setPrice(serviceResponseDto.getPrice());
        service.setDurationInMinutes(serviceResponseDto.getDurationInMinutes());
        service.setEmployee(employee);
        serviceRepository.save(service);

        return mapToDto(service);
    }

    public com.ddimitko.beautyshopproject.entities.Service getServiceById(int serviceId) {
        return  serviceRepository.findById(serviceId).orElseThrow(() -> new RuntimeException("Service not found"));
    }

    public List<ServiceResponseDto> getAllServicesForEmployee(long employeeId) {
        List<com.ddimitko.beautyshopproject.entities.Service> services = serviceRepository.findByEmployeeId(employeeId);
        List<ServiceResponseDto> serviceResponseDtos = new ArrayList<>();
        for (com.ddimitko.beautyshopproject.entities.Service service : services) {
            serviceResponseDtos.add(mapToDto(service));
        }
        return serviceResponseDtos;

    }
}
