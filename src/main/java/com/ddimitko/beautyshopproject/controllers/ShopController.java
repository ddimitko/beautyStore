package com.ddimitko.beautyshopproject.controllers;

import com.ddimitko.beautyshopproject.Dto.responses.EmployeeResponseDto;
import com.ddimitko.beautyshopproject.Dto.responses.ShopResponseDto;
import com.ddimitko.beautyshopproject.aws.S3Service;
import com.ddimitko.beautyshopproject.entities.Employee;
import com.ddimitko.beautyshopproject.entities.Shop;
import com.ddimitko.beautyshopproject.mappers.ShopMapper;
import com.ddimitko.beautyshopproject.mappers.UserMapper;
import com.ddimitko.beautyshopproject.repositories.ShopImageRepository;
import com.ddimitko.beautyshopproject.services.ShopService;
import com.ddimitko.beautyshopproject.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

@RestController
@RequestMapping("/api/shops")
public class ShopController {

    private final ShopService shopService;
    private final ShopMapper shopMapper;
    private final UserMapper userMapper;

    public ShopController(ShopService shopService, ShopMapper shopMapper, UserMapper userMapper) {
        this.shopService = shopService;
        this.shopMapper = shopMapper;
        this.userMapper = userMapper;
    }

    @GetMapping
    public ResponseEntity<List<ShopResponseDto>> getShops(@RequestParam(required = false) String shopName) {
        List<Shop> shops;

        if (shopName != null && !shopName.isEmpty()) {
            shops = shopService.findShopsByName(shopName);
        } else {
            shops = shopService.getAllShops();
        }

        List<ShopResponseDto> shopDtoList = shops.stream()
                .map(shopMapper::convertShopToResponseDto)
                .toList();

        return ResponseEntity.ok(shopDtoList);
    }

    @PutMapping("/{shopId}/upload")
    public ResponseEntity<?> uploadImages(
            @PathVariable Long shopId,
            @RequestParam("file") List<MultipartFile> files,
            @RequestParam(value = "isMain", defaultValue = "false") boolean isMain) {

        try {
            List<String> imageUrls = shopService.uploadShopImages(shopId, files, isMain);
            return ResponseEntity.ok(imageUrls);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(500).body("File upload failed");
        }
    }

    @PutMapping("/{shopId}/setMainImage/{imageId}")
    public ResponseEntity<String> setMainImage(@PathVariable Long shopId, @PathVariable Long imageId) {
        try {
            shopService.setMainImage(shopId, imageId);
            return ResponseEntity.ok("Main image updated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to update main image");
        }
    }

    @GetMapping("/{shopId}")
    public ResponseEntity<ShopResponseDto> getShopById(@PathVariable Long shopId) {
        Shop shop = shopService.getShopById(shopId);
        return ResponseEntity.ok(shopMapper.convertShopToResponseDto(shop));
    }

    @GetMapping("/{shopId}/employees")
    public ResponseEntity<List<EmployeeResponseDto>> getEmployeesByShopId(@PathVariable Long shopId) {
        List<Employee> employeeList = shopService.getEmployeesByShop(shopId);
        List<EmployeeResponseDto> employeeDtoList = new LinkedList<>();
        for (Employee employee : employeeList) {
            employeeDtoList.add(userMapper.mapEmployeeToResponseDto(employee));
        }
        return ResponseEntity.ok(employeeDtoList);
    }

    @PostMapping("/create")
    public ResponseEntity<ShopResponseDto> addShop(@RequestBody Shop shop) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("🛑 Current User: " + auth.getName());
        System.out.println("🛑 User Roles: " + auth.getAuthorities());
        Shop saveShop = shopService.saveShop(shop);
        ShopResponseDto shopDto = shopMapper.convertShopToResponseDto(saveShop);
        return ResponseEntity.status(HttpStatus.CREATED).body(shopDto);
    }


    //TODO: FIX
    @PutMapping("/{shopId}/assign/{userId}")
    public ResponseEntity<Shop> assignEmployeeToShop(@PathVariable long shopId, @PathVariable long userId) {
        shopService.assignEmployeeToShop(shopId, userId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{shopId}/owner/{userId}")
    public ResponseEntity<Shop> changeOwnerToShop(@PathVariable long shopId, @PathVariable long userId) {
        shopService.assignOwnerToShop(shopId, userId);
        return ResponseEntity.ok().build();
    }

}
