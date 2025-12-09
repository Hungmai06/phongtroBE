package com.example.room.service;

import com.example.room.dto.BaseResponse;
import com.example.room.dto.PageResponse;
import com.example.room.dto.request.RoomCreateRequest;
import com.example.room.dto.request.RoomUpdateRequest;
import com.example.room.dto.response.RoomResponse;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public interface RoomService {

    BaseResponse<RoomResponse> createRoom(RoomCreateRequest request, List<MultipartFile> files);

    BaseResponse<RoomResponse> updateRoom(Long id, RoomUpdateRequest request,List<MultipartFile> files);

    BaseResponse<RoomResponse> getRoomById(Long id);


    void deleteRoom(Long id);

    PageResponse<RoomResponse> searchRooms(String q, BigDecimal minPrice, BigDecimal maxPrice, Float minArea, int page, int size, String sort,String type,String status);
}