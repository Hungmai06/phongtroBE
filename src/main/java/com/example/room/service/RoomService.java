package com.example.room.service;

import com.example.room.dto.BaseResponse;
import com.example.room.dto.PageResponse;
import com.example.room.dto.request.RoomCreateRequest;
import com.example.room.dto.request.RoomUpdateRequest;
import com.example.room.dto.response.RoomResponse;

import java.math.BigDecimal;

public interface RoomService {
    BaseResponse<RoomResponse> createRoom(RoomCreateRequest request);
    BaseResponse<RoomResponse> updateRoom(Long id, RoomUpdateRequest request);
    BaseResponse<RoomResponse> getRoomById(Long id);
    BaseResponse<String> deleteRoom(Long id);
    PageResponse<RoomResponse> searchRooms(String q, BigDecimal minPrice, BigDecimal maxPrice, Float minArea, int page, int size, String sort);
}