package com.ddimitko.beautyshopproject.mappers;

import com.ddimitko.beautyshopproject.Dto.responses.EmployeeResponseDto;
import com.ddimitko.beautyshopproject.Dto.responses.ImageDto;
import com.ddimitko.beautyshopproject.Dto.responses.ShopResponseDto;
import com.ddimitko.beautyshopproject.Dto.responses.UserResponseDto;
import com.ddimitko.beautyshopproject.entities.Shop;
import com.ddimitko.beautyshopproject.entities.ShopImage;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ShopMapper {

    private final UserMapper userMapper;

    public ShopMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public ShopResponseDto convertShopToResponseDto(Shop shop) {
        // Fetch all images related to the shop and map them to ImageDTOs
        List<ImageDto> images = shop.getImageURLs().stream()
                .map(image -> new ImageDto(image.getId(), image.getImageUrl(), image.isMain()))
                .toList();

        System.out.println("Shop images: " + images);

        // Mapping the shop owner (if it exists)
        UserResponseDto ownerDto = (shop.getOwner() != null) ? userMapper.mapUserToResponseDto(shop.getOwner()) : null;

        // Mapping the employees
        List<EmployeeResponseDto> employeeList = shop.getEmployees().stream()
                .map(userMapper::mapEmployeeToResponseDto)
                .toList();

        // Return the ShopResponseDto with images instead of separate image URLs & thumbnail
        return new ShopResponseDto(
                shop.getId(),
                shop.getName(),
                shop.getBio(),
                images, // Now passing the ImageDTO list
                shop.getLocation(),
                shop.getPhone(),
                ownerDto,
                employeeList
        );
    }


}
