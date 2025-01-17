package com.mitkov.awsimageanalyzer.services;

import com.mitkov.awsimageanalyzer.exceptions.FileUploadException;
import com.mitkov.awsimageanalyzer.exceptions.ImageSearchException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsRequest;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsResponse;
import software.amazon.awssdk.services.rekognition.model.Image;
import software.amazon.awssdk.services.rekognition.model.S3Object;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImageService {

    @Value("${spring.aws.region}")
    private String region;

    @Value("${spring.aws.s3.bucket-name}")
    private String bucketName;

    private final S3Client s3Client;

    private final RekognitionClient rekognitionClient;

    public void uploadImage(MultipartFile file) {
        String fileName = file.getOriginalFilename();

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
        } catch (IOException e) {
            throw new FileUploadException("Failed to upload file: " + fileName, e);
        }
    }

    public List<String> searchImages(String keyword) {
        try {
            if (keyword == null || keyword.isBlank()) {
                return listAllImageUrls();
            }

            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .build();

            ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);

            return listResponse.contents().stream()
                    .filter(s3Object -> {
                        String fileName = s3Object.key();
                        return isKeywordInImage(fileName, keyword);
                    })
                    .map(s3Object -> String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, s3Object.key()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new ImageSearchException("Error occurred during image search for keyword: " + keyword, e);
        }
    }

    private boolean isKeywordInImage(String fileName, String keyword) {
        try {
            DetectLabelsRequest request = DetectLabelsRequest.builder()
                    .image(Image.builder()
                            .s3Object(S3Object.builder()
                                    .bucket(bucketName)
                                    .name(fileName)
                                    .build())
                            .build())
                    .maxLabels(10)
                    .minConfidence(75F)
                    .build();

            DetectLabelsResponse response = rekognitionClient.detectLabels(request);

            return response.labels().stream()
                    .anyMatch(label -> label.name().equalsIgnoreCase(keyword));
        } catch (Exception e) {
            throw new ImageSearchException("Failed to analyze image: " + fileName, e);
        }
    }

    private List<String> listAllImageUrls() {
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .build();

        ListObjectsV2Response response = s3Client.listObjectsV2(request);

        return response.contents().stream()
                .map(s3Object -> String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, s3Object.key()))
                .collect(Collectors.toList());
    }
}
