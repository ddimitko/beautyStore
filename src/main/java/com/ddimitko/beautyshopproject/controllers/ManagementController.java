package com.ddimitko.beautyshopproject.controllers;

import com.ddimitko.beautyshopproject.Dto.requests.ShopRequestDto;
import com.ddimitko.beautyshopproject.Dto.responses.ShopResponseDto;
import com.ddimitko.beautyshopproject.configs.security.CustomUserDetails;
import com.ddimitko.beautyshopproject.entities.Employee;
import com.ddimitko.beautyshopproject.entities.Shop;
import com.ddimitko.beautyshopproject.entities.User;
import com.ddimitko.beautyshopproject.mappers.ShopMapper;
import com.ddimitko.beautyshopproject.mappers.UserMapper;
import com.ddimitko.beautyshopproject.services.AppointmentService;
import com.ddimitko.beautyshopproject.services.ShopService;
import com.ddimitko.beautyshopproject.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/management")
public class ManagementController {

    private final UserService userService;
    private final ShopService shopService;
    private final AppointmentService appointmentService;

    private final ShopMapper shopMapper;
    private final UserMapper userMapper;

    public ManagementController(UserService userService, ShopService shopService, AppointmentService appointmentService, ShopMapper shopMapper, UserMapper userMapper) {
        this.userService = userService;
        this.shopService = shopService;
        this.appointmentService = appointmentService;
        this.shopMapper = shopMapper;
        this.userMapper = userMapper;
    }

    @GetMapping("/shops")
    public ResponseEntity<?> fetchAllOwnedShops(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        if(customUserDetails == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userService.findUserByEmail(customUserDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<ShopResponseDto> shops = user.getShops().stream()
                .map(shopMapper::convertShopToResponseDto).toList();

        return ResponseEntity.ok(shops);

    }

    @GetMapping("/shop/{shopId}")
    public ResponseEntity<?> getAdminShopById(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                              @PathVariable Long shopId) {
        if (customUserDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
        }

        // Fetch the user based on authenticated details
        User user = userService.getUserById(customUserDetails.getUserId());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        // Check if the user is an OWNER of this shop
        boolean isOwner = user.getShops().stream()
                .anyMatch(shop -> shop.getId() == shopId);

        // For the owner, allow access even if there are no employees
        if (isOwner) {
            // Fetch the shop and return its details
            Shop shop = shopService.getShopById(shopId);
            if (shop == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Shop not found");
            }
            return ResponseEntity.ok(shopMapper.convertShopToResponseDto(shop));
        }

        // Check if the user is an EMPLOYEE assigned to this shop
        Employee employee;
        try {
            employee = userService.getEmployeeById(customUserDetails.getUserId());
        } catch (RuntimeException e) {
            // Catch exception when there are no employees or other issues
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Employee not found for this shop");
        }

        // Check if the employee is assigned to the requested shop
        boolean isEmployeeOfShop = (employee != null && employee.getShop() != null &&
                employee.getShop().getId() == shopId);

        // If the user is neither an OWNER nor an EMPLOYEE, deny access
        if (!isOwner && !isEmployeeOfShop) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to access this shop");
        }

        // Fetch and return the shop details
        Shop shop = shopService.getShopById(shopId);
        if (shop == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Shop not found");
        }

        return ResponseEntity.ok(shopMapper.convertShopToResponseDto(shop));
    }


    @PatchMapping("/shop/{shopId}")
    public ResponseEntity updateShop(@AuthenticationPrincipal CustomUserDetails userDetails,
                                        @PathVariable Long shopId,
                                        @RequestBody ShopRequestDto shopRequestDto) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
        }

        // Check if the user is the owner
        User owner = userService.getUserById(userDetails.getUserId());
        if (owner == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        // Validate shop ownership
        boolean isOwner = owner.getShops().stream().anyMatch(shop -> shop.getId() == shopId);
        if (!isOwner) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to update this shop");
        }

        // Perform the update
        shopService.updateShop(shopId, owner, shopRequestDto);
        return ResponseEntity.ok("Shop updated successfully");
    }
}
