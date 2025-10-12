package com.example.room.service;

import org.springframework.web.multipart.MultipartFile;

public interface IStorageService {
    String storeFile(MultipartFile file);
    void deleteFile(String fileName);
}