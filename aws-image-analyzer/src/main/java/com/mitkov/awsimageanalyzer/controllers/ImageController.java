package com.mitkov.awsimageanalyzer.controllers;

import com.mitkov.awsimageanalyzer.services.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/images")
public class ImageController {

    private final ImageService imageService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
        imageService.uploadImage(file);
        return ResponseEntity.ok("File uploaded successfully");
    }

    @GetMapping("/search")
    public List<String> searchImages(@RequestParam(value = "keyword", required = false) String keyword) {
        return imageService.searchImages(keyword);
    }
}
