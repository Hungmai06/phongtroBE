package com.example.room.service.Impl;

import com.example.room.service.IStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class ImageStorageService implements IStorageService {
    private final Path storageFolder = Paths.get("uploads");

    public ImageStorageService() {
        try {
            Files.createDirectories(storageFolder);
        } catch (IOException exception) {
            throw new RuntimeException("Không thể khởi tạo thư mục lưu trữ", exception);
        }
    }

    @Override
    public String storeFile(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Không thể lưu file rỗng.");
            }
            // Tạo tên file ngẫu nhiên để tránh trùng lặp
            String fileExtension = getFileExtension(file.getOriginalFilename());
            String generatedFileName = UUID.randomUUID().toString().replace("-", "") + "." + fileExtension;

            Path destinationFilePath = this.storageFolder.resolve(Paths.get(generatedFileName)).normalize().toAbsolutePath();

            // Lưu file
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFilePath, StandardCopyOption.REPLACE_EXISTING);
            }
            return generatedFileName; // Trả về tên file đã lưu
        } catch (IOException exception) {
            throw new RuntimeException("Lỗi khi lưu file.", exception);
        }
    }

    @Override
    public void deleteFile(String fileName) {
        try {
            Path filePath = storageFolder.resolve(Paths.get(fileName)).normalize().toAbsolutePath();
            Files.deleteIfExists(filePath);
        } catch (IOException exception) {
            throw new RuntimeException("Lỗi khi xóa file: " + fileName, exception);
    }
}

    private String getFileExtension(String fileName) {
        int lastIndexOf = fileName.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return ""; // No extension
        }
        return fileName.substring(lastIndexOf + 1);
    }
}