package com.ddimitko.beautyshopproject.services;

import com.ddimitko.beautyshopproject.Dto.requests.ShopRequestDto;
import com.ddimitko.beautyshopproject.entities.*;
import com.ddimitko.beautyshopproject.nomenclatures.Roles;
import com.ddimitko.beautyshopproject.repositories.EmployeeRepository;
import com.ddimitko.beautyshopproject.repositories.ShopImageRepository;
import com.ddimitko.beautyshopproject.repositories.ShopRepository;
import com.ddimitko.beautyshopproject.repositories.UserRepository;
import jakarta.transaction.Transactional;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ShopService {

    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final ShopImageRepository shopImageRepository;

    public ShopService(ShopRepository shopRepository, UserRepository userRepository, EmployeeRepository employeeRepository, ShopImageRepository shopImageRepository) {
        this.shopRepository = shopRepository;
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.shopImageRepository = shopImageRepository;
    }

    public List<Shop> getAllShops() {
        return shopRepository.findAll();
    }

    public List<Shop> findShopsByName(String name) {
        return shopRepository.findByNameContainingIgnoreCase(name);
    }

    public Shop findShopById(Long id) {
        return shopRepository.findById(id).orElseThrow(() -> new RuntimeException("Shop not found"));
    }

    //@Cacheable(key = "#id", value = "shop")
    public Shop getShopById(Long id) {
        return shopRepository.findById(id).orElseThrow(() -> new RuntimeException("Shop not found"));
    }

    public Shop saveShop(Shop shop) {
        return shopRepository.save(shop);
    }

    public void setMainImage(Long shopId, Long imageId) {
        // Get all images for the shop and update only if necessary
        List<ShopImage> images = shopImageRepository.findByShopId(shopId);

        ShopImage newMainImage = null;
        boolean needsUpdate = false;

        for (ShopImage image : images) {
            if (image.getId().equals(imageId)) {
                if (!image.isMain()) { // Only update if it's not already main
                    image.setMain(true);
                    newMainImage = image;
                    needsUpdate = true;
                }
            } else if (image.isMain()) { // Unmark the existing main image
                image.setMain(false);
                needsUpdate = true;
            }
        }

        // Save only if changes were made
        if (needsUpdate) {
            shopImageRepository.saveAll(images);
        }
    }

    @Transactional
    public List<String> uploadShopImages(Long shopId, List<MultipartFile> files, boolean isMain) throws IOException {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("No files selected for upload.");
        }

        List<String> imageUrls = new ArrayList<>();
        String uploadDir = "uploads/shopGalleries/shop-" + shopId + "/";

        Files.createDirectories(Paths.get(uploadDir)); // Ensure directory exists

        // Validate number of images
        if (files.size() > 3) {
            throw new IllegalArgumentException("You can upload a maximum of 3 images.");
        }

        Shop shop = findShopById(shopId);

        for (MultipartFile file : files) {
            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
                throw new IllegalArgumentException("Only .jpg, .jpeg, and .png files are allowed.");
            }

            // Generate unique file name
            String fileName = UUID.randomUUID() + ".jpg";
            Path filePath = Paths.get(uploadDir + fileName);

            // Resize and compress the image
            BufferedImage originalImage = ImageIO.read(file.getInputStream());
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            Thumbnails.of(originalImage)
                    .size(800, 600) // Resize to 800x600
                    .outputFormat("jpg")
                    .outputQuality(0.8)
                    .toOutputStream(outputStream);

            byte[] resizedImageBytes = outputStream.toByteArray();
            Files.write(filePath, resizedImageBytes);

            String serverUrl = "http://localhost:8080/";  // Change to your actual backend URL
            String imageUrl = serverUrl + "uploads/shopGalleries/shop-" + shopId + "/" + fileName;
            imageUrls.add(imageUrl);

            // Create and save the ShopImage entity
            ShopImage shopImage = new ShopImage(imageUrl, shop, false);
            shop.getImageURLs().add(shopImage);
        }

        // Handle `isMain` logic
        if (isMain && !imageUrls.isEmpty()) {
            shop.getImageURLs().forEach(shopImage -> {
                if (shopImage.isMain()) {
                    shopImage.setMain(false);
                    shopImageRepository.save(shopImage);
                }
            });
        }


        // Save shop with updated images
        saveShop(shop);

        return imageUrls;
    }

    @Transactional
    public void assignEmployeeToShop(Long shopId, Long userId){
        Shop shop = shopRepository.findById(shopId).orElseThrow(() -> new RuntimeException("Shop not found"));
        User user = userRepository.findById(userId).orElseThrow(()-> new RuntimeException("User not found"));

        if(!employeeRepository.existsByUserId(user.getId())) {
            Employee employee = new Employee();
            employee.setShop(shop);
            employee.setUser(user);
            user.setRole(Roles.EMPLOYEE);
            employeeRepository.save(employee);
        }
        else{
            throw new RuntimeException("Employee already assigned to shop");
        }

    }

    @Transactional
    public void assignOwnerToShop(Long shopId, Long userId){
        Shop shop = shopRepository.findById(shopId).orElseThrow(() -> new RuntimeException("Shop not found"));
        User user = userRepository.findById(userId).orElseThrow(()-> new RuntimeException("User not found"));

        // Check if the user already owns this shop
        if (user.getShops().contains(shop)) {
            throw new RuntimeException("User already owns this shop");
        }

        // Set user as owner and update their shops list
        shop.setOwner(user);
        user.getShops().add(shop);
        user.setRole(Roles.OWNER);
        userRepository.save(user);
    }

    public List<Employee> getEmployeesByShop(Long shopId) {
        /*Shop shop = shopRepository.findByIdWithEmployees(shopId);
        return shop != null ? shop.getEmployees() : Collections.emptyList();
        */
        return null; //employeeRepository.findByShopId(shopId);
    }

    @Transactional
    public Shop updateShop(Long shopId, User owner, ShopRequestDto shopRequestDto) {
        Shop shop = shopRepository.findById(shopId).orElseThrow(() -> new RuntimeException("Shop not found"));
        if (!shop.getOwner().equals(owner)) {
            throw new RuntimeException("Access denied: You do not own this shop.");
        }
        if (shopRequestDto.getShopName() != null) {
            shop.setName(shopRequestDto.getShopName());
        }
        if (shopRequestDto.getShopAddress() != null) {
            shop.setLocation(shopRequestDto.getShopAddress());
        }
        if (shopRequestDto.getShopPhone() != null) {
            shop.setPhone(shopRequestDto.getShopPhone());
        }
        if (shopRequestDto.getShopBio() != null) {
            shop.setBio(shopRequestDto.getShopBio());
        }
        return shopRepository.save(shop);
    }
}
