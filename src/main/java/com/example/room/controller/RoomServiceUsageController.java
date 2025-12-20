package com.example.room.controller;


import com.example.room.dto.BaseResponse;
import com.example.room.dto.PageResponse;
import com.example.room.dto.request.RoomServiceUsageRequest;
import com.example.room.dto.response.RoomServiceUsageResponse;
import com.example.room.service.RoomServiceUsageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/room-service-usages")
@RequiredArgsConstructor
@Tag(name = " API Room Service Usage", description = "Quản lý so lieu  sử dụng dịch vụ theo phòng trọ (điện, nước, wifi, ...)")
public class RoomServiceUsageController {

    private final RoomServiceUsageService roomServiceUsageService;

    @Operation(summary = "Tạo mới bản ghi sử dụng dịch vụ")
    @PostMapping("")
    public BaseResponse<RoomServiceUsageResponse> create(
            @RequestBody RoomServiceUsageRequest request) {
        return roomServiceUsageService.create(request);
    }

    @Operation(summary = "Cập nhật bản ghi sử dụng dịch vụ")
    @PutMapping("/{id}")
    public BaseResponse<RoomServiceUsageResponse> update(
            @PathVariable Long id,
            @RequestBody RoomServiceUsageRequest request) {
        return roomServiceUsageService.update(id, request);
    }

    @Operation(summary = "Xóa bản ghi sử dụng dịch vụ")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        roomServiceUsageService.delete(id);
    }

    @Operation(summary = "Xem chi tiết bản ghi sử dụng dịch vụ")
    @GetMapping("/{id}")
    public BaseResponse<RoomServiceUsageResponse> getById(@PathVariable Long id) {
        return roomServiceUsageService.getById(id);
    }
    @GetMapping("/room/{roomId}")
    public BaseResponse<List<RoomServiceUsageResponse>> getByRoomIdAndMonth(@PathVariable Long roomId, @RequestParam String month) {
        return roomServiceUsageService.getByRoomIdAndMonth(roomId, month);
    }


    @Operation(summary = "Danh sách sử dụng dịch vụ (phân trang)")
    @GetMapping("")
    public PageResponse<RoomServiceUsageResponse> getAll(
            @RequestParam String month,
            @RequestParam Integer page,
            @RequestParam Integer size) {

        return roomServiceUsageService.getAll(month, page, size);
    }
}
