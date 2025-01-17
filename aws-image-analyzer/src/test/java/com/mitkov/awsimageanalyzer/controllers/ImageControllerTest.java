package com.mitkov.awsimageanalyzer.controllers;

import com.mitkov.awsimageanalyzer.services.ImageService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ImageService imageService;

    @Test
    public void testUploadImage() throws Exception {
        doNothing().when(imageService).uploadImage(Mockito.any(MultipartFile.class));

        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        mockMvc.perform(multipart("/images/upload")
                        .file(mockFile))
                .andExpect(status().isOk())
                .andExpect(content().string("File uploaded successfully"));

        verify(imageService, times(1)).uploadImage(any(MultipartFile.class));
    }

    @Test
    public void testSearchImagesWithKeyword() throws Exception {
        when(imageService.searchImages("cat")).thenReturn(List.of(
                "https://bucket.s3.region.amazonaws.com/cat1.jpg",
                "https://bucket.s3.region.amazonaws.com/cat2.jpg"
        ));

        mockMvc.perform(get("/images/search")
                        .param("keyword", "cat"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("https://bucket.s3.region.amazonaws.com/cat1.jpg"))
                .andExpect(jsonPath("$[1]").value("https://bucket.s3.region.amazonaws.com/cat2.jpg"));

        verify(imageService, times(1)).searchImages("cat");
    }

    @Test
    public void testSearchImagesWithoutKeyword() throws Exception {
        when(imageService.searchImages(null)).thenReturn(List.of(
                "https://bucket.s3.region.amazonaws.com/image1.jpg",
                "https://bucket.s3.region.amazonaws.com/image2.jpg"
        ));

        mockMvc.perform(get("/images/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("https://bucket.s3.region.amazonaws.com/image1.jpg"))
                .andExpect(jsonPath("$[1]").value("https://bucket.s3.region.amazonaws.com/image2.jpg"));

        verify(imageService, times(1)).searchImages(null);
    }
}
