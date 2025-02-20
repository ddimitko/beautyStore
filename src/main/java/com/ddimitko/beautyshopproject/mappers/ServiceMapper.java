package com.ddimitko.beautyshopproject.mappers;

import com.ddimitko.beautyshopproject.Dto.responses.ServiceResponseDto;
import com.ddimitko.beautyshopproject.entities.Service;
import org.springframework.stereotype.Component;

@Component
public class ServiceMapper {

    public ServiceResponseDto mapServiceToResponseDto(Service service) {
        return new ServiceResponseDto(service.getId(), service.getName(), service.getDescription(), service.getPrice(), service.getDurationInMinutes());
    }

}
