package com.mitkov.awsimageanalyzer.services;

import com.mitkov.awsimageanalyzer.exceptions.FileUploadException;
import com.mitkov.awsimageanalyzer.exceptions.ImageSearchException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsRequest;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsResponse;
import software.amazon.awssdk.services.rekognition.model.Label;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ImageServiceTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private RekognitionClient rekognitionClient;

    @InjectMocks
    private ImageService imageService;

    @Test
    public void testUploadImageSuccess() throws IOException {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("test-image.jpg");
        when(mockFile.getBytes()).thenReturn("test content".getBytes());

        assertDoesNotThrow(() -> imageService.uploadImage(mockFile));

        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    public void testUploadImageFailure() throws IOException {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("test-image.jpg");
        when(mockFile.getBytes()).thenThrow(new IOException("Test exception"));

        FileUploadException exception = assertThrows(FileUploadException.class, () -> imageService.uploadImage(mockFile));
        assertTrue(exception.getMessage().contains("Failed to upload file"));

        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    public void testSearchImagesWithoutKeyword() {
        ReflectionTestUtils.setField(imageService, "bucketName", "bucket-name");
        ReflectionTestUtils.setField(imageService, "region", "region");

        S3Object s3Object = S3Object.builder()
                .key("image1.jpg")
                .build();

        ListObjectsV2Response mockResponse = mock(ListObjectsV2Response.class);
        when(mockResponse.contents()).thenReturn(List.of(s3Object));

        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(mockResponse);

        List<String> result = imageService.searchImages(null);

        assertNotNull(result);
        assertTrue(result.contains("https://bucket-name.s3.region.amazonaws.com/image1.jpg"));
    }

    @Test
    public void testSearchImagesWithKeyword() {
        ReflectionTestUtils.setField(imageService, "bucketName", "bucket-name");
        ReflectionTestUtils.setField(imageService, "region", "region");

        ListObjectsV2Response mockS3Response = ListObjectsV2Response.builder()
                .contents(S3Object.builder().key("cat.jpg").build())
                .build();
        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(mockS3Response);

        DetectLabelsResponse mockRekognitionResponse = DetectLabelsResponse.builder()
                .labels(Label.builder().name("cat").confidence(99.0f).build())
                .build();
        when(rekognitionClient.detectLabels(any(DetectLabelsRequest.class))).thenReturn(mockRekognitionResponse);

        List<String> result = imageService.searchImages("cat");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains("https://bucket-name.s3.region.amazonaws.com/cat.jpg"));

        verify(s3Client, times(1)).listObjectsV2(any(ListObjectsV2Request.class));
        verify(rekognitionClient, times(1)).detectLabels(any(DetectLabelsRequest.class));
    }

    @Test
    public void testSearchImagesThrowsException() {
        ReflectionTestUtils.setField(imageService, "bucketName", "bucket-name");
        ReflectionTestUtils.setField(imageService, "region", "region");

        ListObjectsV2Response mockS3Response = ListObjectsV2Response.builder()
                .contents(S3Object.builder().key("invalid.jpg").build())
                .build();
        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(mockS3Response);

        when(rekognitionClient.detectLabels(any(DetectLabelsRequest.class)))
                .thenThrow(SdkClientException.create("Test Rekognition exception"));

        ImageSearchException exception = assertThrows(ImageSearchException.class, () -> imageService.searchImages("invalid"));
        assertTrue(exception.getMessage().contains("Error occurred during image search for keyword: invalid"));

        verify(s3Client, times(1)).listObjectsV2(any(ListObjectsV2Request.class));
        verify(rekognitionClient, times(1)).detectLabels(any(DetectLabelsRequest.class));
    }
}
