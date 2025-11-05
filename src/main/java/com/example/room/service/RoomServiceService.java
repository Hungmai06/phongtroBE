package com.example.room.service;

import com.example.room.dto.BaseResponse;
import com.example.room.dto.PageResponse;
import com.example.room.dto.request.RoomServiceCreateRequest;
import com.example.room.dto.response.RoomServiceResponse;


public interface RoomServiceService {
    BaseResponse<RoomServiceResponse> create(RoomServiceCreateRequest request);
    BaseResponse<RoomServiceResponse> update(Long id, RoomServiceCreateRequest request);
    void delete(Long id);
    BaseResponse<RoomServiceResponse> findById(Long id);
    PageResponse<RoomServiceResponse> findAll(Integer page, Integer size);
}
