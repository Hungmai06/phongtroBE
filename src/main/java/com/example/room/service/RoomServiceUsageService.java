package com.example.room.service;

import com.example.room.dto.BaseResponse;
import com.example.room.dto.PageResponse;
import com.example.room.dto.request.RoomServiceUsageRequest;
import com.example.room.dto.response.RoomServiceUsageResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

public interface RoomServiceUsageService {
    BaseResponse<RoomServiceUsageResponse> create(RoomServiceUsageRequest request);
    BaseResponse<RoomServiceUsageResponse>  update(Long id, RoomServiceUsageRequest request);
    void delete(Long id);
    BaseResponse<RoomServiceUsageResponse>  getById(Long id);
    PageResponse<RoomServiceUsageResponse> getAll(String month, Integer page, Integer size);
}
