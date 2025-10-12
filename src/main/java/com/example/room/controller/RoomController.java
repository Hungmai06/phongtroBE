package com.example.room.controller;

import com.example.room.dto.BaseResponse;
import com.example.room.dto.PageResponse;
import com.example.room.dto.request.RoomCreateRequest;
import com.example.room.dto.request.RoomUpdateRequest;
import com.example.room.dto.response.RoomResponse;
import com.example.room.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rooms")
@Tag(name = "API ROOM", description = "API cho quản lý phòng trọ")
public class RoomController {

    private final RoomService roomService;

    @PostMapping("")
    @Operation(summary = "Đăng một phòng trọ mới")
    public BaseResponse<RoomResponse> createRoom(@Valid @RequestBody RoomCreateRequest request) {
        return roomService.createRoom(request);
    }

    @GetMapping("")
    @Operation(summary = "Lấy danh sách phòng trọ có phân trang và lọc")
    public PageResponse<RoomResponse> searchRooms(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Float minArea,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "asc") String sort
    ) {
        return roomService.searchRooms(q, minPrice, maxPrice, minArea, page, size, sort);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy thông tin chi tiết của một phòng")
    public BaseResponse<RoomResponse> getRoomById(@PathVariable Long id) {
        return roomService.getRoomById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật thông tin của một phòng")
    public BaseResponse<RoomResponse> updateRoom(@PathVariable Long id, @Valid @RequestBody RoomUpdateRequest request) {
        return roomService.updateRoom(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa một phòng (xóa mềm)")
    public BaseResponse<String> deleteRoom(@PathVariable Long id) {
        return roomService.deleteRoom(id);
    }
}