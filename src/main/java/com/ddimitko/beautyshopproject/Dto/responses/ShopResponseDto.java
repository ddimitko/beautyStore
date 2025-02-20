package com.ddimitko.beautyshopproject.Dto.responses;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ShopResponseDto {

    private Long id;
    private String name;
    private String bio;
    private List<ImageDto> images = new ArrayList<>();
    private String location;
    private String phone;
    private UserResponseDto owner;
    private List<EmployeeResponseDto> employeeList = new ArrayList<>();

    public ShopResponseDto(Long id, String name, String bio, List<ImageDto> images, String location, String phone, UserResponseDto owner, List<EmployeeResponseDto> employeeList) {
        this.id = id;
        this.name = name;
        this.bio = bio;
        this.images = images;
        this.location = location;
        this.phone = phone;
        if(owner != null) {
            this.owner = owner;
        }
        this.employeeList = employeeList;
    }

}
