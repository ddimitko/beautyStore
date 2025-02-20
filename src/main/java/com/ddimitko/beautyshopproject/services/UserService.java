package com.ddimitko.beautyshopproject.services;

import com.ddimitko.beautyshopproject.Dto.requests.SignupRequest;
import com.ddimitko.beautyshopproject.Dto.responses.UserResponseDto;
import com.ddimitko.beautyshopproject.Dto.responses.EmployeeResponseDto;
import com.ddimitko.beautyshopproject.aws.S3Service;
import com.ddimitko.beautyshopproject.entities.Employee;
import com.ddimitko.beautyshopproject.nomenclatures.Roles;
import com.ddimitko.beautyshopproject.entities.User;
import com.ddimitko.beautyshopproject.mappers.UserMapper;
import com.ddimitko.beautyshopproject.repositories.EmployeeRepository;
import com.ddimitko.beautyshopproject.repositories.UserRepository;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final S3Service s3Service;

    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    public UserService(EmployeeRepository employeeRepository, UserRepository userRepository, UserMapper userMapper, S3Service s3Service) {
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.s3Service = s3Service;
    }

    public Optional<User> findUserByEmail(String email){


        return userRepository.findByEmail(email);

    }

    public boolean existsByEmail(String email){
        return userRepository.existsByEmail(email);
    }

    //Customers' logic
    public List<UserResponseDto> findAllCustomers() {
        List<User> users = userRepository.findAllByRole(Roles.USER);
        List<UserResponseDto> userResponseDtos = new ArrayList<>();
        for (User customer : users) {
            UserResponseDto dto = userMapper.mapUserToResponseDto(customer);
            userResponseDtos.add(dto);
        }
        return userResponseDtos;
    }

    public User getUserById(long userId){
        return userRepository.findById(userId).orElseThrow(()-> new RuntimeException("User not found") );
    }

    public User saveUser(SignupRequest signupRequest) {
        if(existsByEmail(signupRequest.getEmail())){
            throw new RuntimeException("User with that email already exists.");
        }
        User user = new User();
        user.setFirstName(signupRequest.getFirstName());
        user.setLastName(signupRequest.getLastName());
        user.setEmail(signupRequest.getEmail());
        user.setPassword(passwordEncoder().encode(signupRequest.getPassword()));
        user.setRole(Roles.USER);
        return userRepository.save(user);
    }

    public String uploadProfilePicture(Long userId, MultipartFile file) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String uploadDir = "uploads/profilePhotos/";
        String fileName = userId + "-" + UUID.randomUUID() + ".jpg"; // Unique file name
        Path filePath = Paths.get(uploadDir + fileName);

        // Resize and compress the image before saving
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        Thumbnails.of(originalImage)
                .size(300, 300)  // Resize to 300x300 pixels
                .outputFormat("jpg")  // Convert to JPG
                .outputQuality(0.75)  // Compress to 75% quality
                .toOutputStream(outputStream);

        // Save the compressed image to the server
        byte[] resizedImageBytes = outputStream.toByteArray();
        Files.createDirectories(filePath.getParent()); // Ensure directory exists
        Files.write(filePath, resizedImageBytes);

        // Delete old profile picture if exists
        if (user.getProfilePicture() != null) {
            deleteProfilePicture(userId, user.getProfilePicture());
        }

        // Store file path in DB
        String serverUrl = "http://localhost:8080/";  // Change to your actual backend URL
        String imageUrl = serverUrl + "uploads/profilePhotos/" + fileName;
        user.setProfilePicture(imageUrl);
        userRepository.save(user);

        return imageUrl; // Return local URL
    }



    public void deleteProfilePicture(Long userId, String fileUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (fileUrl != null) {
            // Extract file path from URL
            String filePath = "uploads" + fileUrl.replace("/uploads", "");
            File file = new File(filePath);

            // Delete the file from local storage
            if (file.exists() && file.delete()) {
                System.out.println("Deleted profile picture: " + filePath);
            } else {
                System.out.println("Profile picture not found or unable to delete: " + filePath);
            }
        }

        // Remove profile picture from the database
        user.setProfilePicture(null);
        userRepository.save(user);
    }


    public void deleteCustomerById(long customerId) {
        userRepository.deleteById(customerId);
    }

    //Employees' logic
    public List<EmployeeResponseDto> findAllEmployees() {
        List<Employee> employees = employeeRepository.findAll();
        List<EmployeeResponseDto> employeeResponseDtos = new ArrayList<>();
        for (Employee employee : employees) {
            EmployeeResponseDto dto = userMapper.mapEmployeeToResponseDto(employee);
            employeeResponseDtos.add(dto);
        }
        return employeeResponseDtos;
    }

    public Employee getEmployeeById(long userId) {
        return employeeRepository.findById(userId).orElseThrow(() -> new RuntimeException("Employee not found"));
    }

    public void deleteEmployeeById(long employeeId) {
        employeeRepository.deleteById(employeeId);
    }
}
