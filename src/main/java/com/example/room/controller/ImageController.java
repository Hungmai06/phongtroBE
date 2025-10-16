package com.example.room.controller;

import com.example.room.dto.BaseResponse;
import com.example.room.service.IStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/images")
@Tag(name = "API IMAGE", description = "API cho upload hình ảnh")
public class ImageController {

    private final IStorageService storageService;

    @PostMapping(value = "/upload", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @Operation(summary = "Upload nhiều hình ảnh")
    public BaseResponse<List<String>> uploadImages(@RequestParam("files") List<MultipartFile> files) {
        
        List<String> generatedFileNames = files.stream()
                .map(storageService::storeFile)
                .collect(Collectors.toList());

        // Trả về danh sách các URL của ảnh đã upload
        List<String> imageUrls = generatedFileNames.stream()
                .map(fileName -> "/api/images/" + fileName)
                .collect(Collectors.toList());

        return BaseResponse.<List<String>>builder()
                .code(200)
                .message("Upload ảnh thành công")
                .data(imageUrls)
                .build();
    }
    @DeleteMapping("/{fileName}")
        @Operation(summary = "Xóa một hình ảnh")
        public BaseResponse<String> deleteImage(@PathVariable String fileName) {
        storageService.deleteFile(fileName);
        return BaseResponse.<String>builder()
                .code(200)
                .message("Đã xóa file thành công")
                .data(fileName)
                .build();
        }
}