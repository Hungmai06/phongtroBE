package com.example.room.service.Impl;

import com.example.room.service.IStorageService;
import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
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

    private boolean isImageFile(MultipartFile file) {
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        return extension != null && (
               extension.equalsIgnoreCase("png") ||
               extension.equalsIgnoreCase("jpg") ||
               extension.equalsIgnoreCase("jpeg") ||
               extension.equalsIgnoreCase("bmp")
        );
    }

    @Override
    public String storeFile(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Không thể lưu file rỗng.");
            }
            if (!isImageFile(file)) {
                throw new RuntimeException("Chỉ có thể upload file ảnh (png, jpg, jpeg, bmp).");
            }

            String fileExtension = FilenameUtils.getExtension(file.getOriginalFilename());
            String generatedFileName = UUID.randomUUID().toString().replace("-", "") + "." + fileExtension;

            Path destinationFilePath = this.storageFolder.resolve(Paths.get(generatedFileName))
                    .normalize()
                    .toAbsolutePath();

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFilePath, StandardCopyOption.REPLACE_EXISTING);
            }

            // Trả về đường dẫn public
            return "/uploads/" + generatedFileName;

        } catch (IOException exception) {
            throw new RuntimeException("Lỗi khi lưu file.", exception);
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