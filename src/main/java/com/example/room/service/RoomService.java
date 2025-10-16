package com.example.room.service;

import com.example.room.dto.PageResponse;
import com.example.room.dto.request.RoomCreateRequest;
import com.example.room.dto.request.RoomUpdateRequest;
import com.example.room.dto.response.RoomResponse;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

public interface RoomService {
    // Trả về trực tiếp RoomResponse
    RoomResponse createRoom(RoomCreateRequest request);

    // Trả về trực tiếp RoomResponse
    RoomResponse updateRoom(Long id, RoomUpdateRequest request);

    // Trả về trực tiếp RoomResponse
    RoomResponse getRoomById(Long id);

    // Không trả về gì (void)
    void deleteRoom(Long id);

    // PageResponse đã là một cấu trúc chuẩn cho phân trang, giữ nguyên
    PageResponse<RoomResponse> searchRooms(String q, BigDecimal minPrice, BigDecimal maxPrice, Float minArea, int page, int size, String sort);
}