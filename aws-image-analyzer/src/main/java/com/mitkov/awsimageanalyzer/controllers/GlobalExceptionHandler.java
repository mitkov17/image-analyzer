package com.mitkov.awsimageanalyzer.controllers;

import com.mitkov.awsimageanalyzer.exceptions.FileUploadException;
import com.mitkov.awsimageanalyzer.exceptions.ImageSearchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<String> handleFileUploadException(FileUploadException e) {
        HttpStatus status = e.getCause() instanceof IOException
                ? HttpStatus.INTERNAL_SERVER_ERROR
                : HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(e.getMessage(), status);
    }

    @ExceptionHandler(ImageSearchException.class)
    public ResponseEntity<String> handleImageSearchException(ImageSearchException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
