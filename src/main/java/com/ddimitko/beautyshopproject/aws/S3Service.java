package com.ddimitko.beautyshopproject.aws;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.UUID;

@Service
public class S3Service {

    private final String bucketName;
    private final S3Client s3Client;
    private final S3Presigner presigner;
    private final Region awsRegion;

    public S3Service(
            @Value("${aws.s3.bucket-name}") String bucketName,
            @Value("${aws.s3.access-key}") String accessKey,
            @Value("${aws.s3.secret-key}") String secretKey,
            @Value("${aws.s3.region}") String region) {

        this.bucketName = bucketName;
        this.awsRegion = Region.of(region);

        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        this.s3Client = S3Client.builder()
                .region(awsRegion)
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();

        this.presigner = S3Presigner.builder()
                .region(awsRegion)
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    public String uploadFileToShop(Long shopId, File file) {
        String fileName = UUID.randomUUID() + "_" + file.getName();  // Make the file name unique

        // Generate presigned URL for the shop image upload
        String presignedUrl = generateShopImageUploadUrl(shopId, fileName);

        // Return the presigned URL to the frontend, where it will upload the file
        return presignedUrl;
    }

    public String uploadProfilePicture(Long userId, MultipartFile file) throws IOException {

        String fileName = file.getOriginalFilename();

        // Convert MultipartFile to BufferedImage
        BufferedImage originalImage = ImageIO.read(file.getInputStream());

        // Resize and compress the image
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Thumbnails.of(originalImage)
                .size(300, 300)  // Resize to 300x300 pixels
                .outputFormat("jpg")  // Convert to JPG
                .outputQuality(0.75)  // Compress to 75% quality
                .toOutputStream(outputStream);

        byte[] resizedImageBytes = outputStream.toByteArray();
        String presignedKey = generateProfilePictureUploadUrl(userId, fileName);

        return presignedKey;
    }

    public void deleteProfilePicture(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) return;

        // Extract file key from URL
        String fileKey = fileUrl.substring(fileUrl.indexOf("profilePictures/"));

        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .build());
    }

    public String generateProfilePictureUploadUrl(Long userId, String fileName) {
        String key = "profilePictures/" + userId + "-" + UUID.randomUUID() + "-" + fileName;

        // Define allowed content types for shop images
        String contentType; // Default type
        if (fileName.endsWith(".png")) {
            contentType = "image/png";
        } else if (fileName.endsWith(".jpg")) {
            contentType = "image/jpg";
        } else {
            contentType = "image/jpeg";
        }

        PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(
                PutObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(10)) // URL valid for 10 mins
                        .putObjectRequest(
                                b -> b.bucket(bucketName)
                                        .key(key)
                                        .contentType(contentType) // Optional: Restrict file type
                        )
                        .build()
        );

        return presignedRequest.url().toString();
    }

    public String generateShopImageUploadUrl(Long shopId, String fileName) {
        String shopFolder = "shopGalleries/shop-" + shopId + "/";  // Folder for the shop (using shopId)
        String objectKey = shopFolder + fileName;  // Full path in S3

        // Define allowed content types for shop images
        String contentType; // Default type
        if (fileName.endsWith(".png")) {
            contentType = "image/png";
        } else if (fileName.endsWith(".jpg")) {
            contentType = "image/jpg";
        } else {
            contentType = "image/jpeg";
        }

        // Generate a presigned URL for the upload
        PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(
                PutObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(10)) // URL valid for 10 minutes
                        .putObjectRequest(
                                b -> b.bucket(bucketName)
                                        .key(objectKey)
                                        .contentType(contentType) // Content type based on file
                        )
                        .build()
        );

        // Return the presigned URL for the client to use
        return presignedRequest.url().toString();
    }
    
}
