package com.ddimitko.beautyshopproject.mappers;

import com.ddimitko.beautyshopproject.Dto.responses.UserResponseDto;
import com.ddimitko.beautyshopproject.Dto.responses.EmployeeResponseDto;
import com.ddimitko.beautyshopproject.entities.Employee;
import com.ddimitko.beautyshopproject.entities.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    private final ServiceMapper serviceMapper;

    public UserMapper(ServiceMapper serviceMapper) {
        this.serviceMapper = serviceMapper;
    }

    public UserResponseDto mapUserToResponseDto(User user) {
        return new UserResponseDto(user.getId(), user.getFirstName(), user.getLastName(),
                user.getEmail(), user.getProfilePicture(), "ROLE_" + user.getRole().name());
    }

    public EmployeeResponseDto mapEmployeeToResponseDto(Employee employee) {
        return new EmployeeResponseDto(employee.getUser().getId(), employee.getShop().getId(), employee.getUser().getFirstName() + " " + employee.getUser().getLastName(),
                employee.getUser().getEmail(), employee.getServices().stream().map(serviceMapper::mapServiceToResponseDto).toList());
    }

}
