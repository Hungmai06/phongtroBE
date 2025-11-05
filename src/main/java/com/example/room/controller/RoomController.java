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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rooms")
@Tag(name = "API ROOM", description = "API cho quản lý phòng trọ")
public class RoomController {

    private final RoomService roomService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Đăng một phòng trọ mới (chỉ thông tin, không kèm ảnh)")
    @PreAuthorize("hasAuthority('OWNER')")
    public ResponseEntity<BaseResponse<RoomResponse>> createRoom(
            @Valid @RequestBody RoomCreateRequest request
    ) {
        RoomResponse newRoom = roomService.createRoom(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                BaseResponse.<RoomResponse>builder()
                        .code(HttpStatus.CREATED.value())
                        .message("Tạo phòng trọ thành công")
                        .data(newRoom)
                        .build()
        );
    }

    @GetMapping("")
    @Operation(summary = "Lấy danh sách phòng trọ có phân trang và lọc")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'OWNER', 'RENTER')")
    public ResponseEntity<PageResponse<RoomResponse>> searchRooms(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Float minArea,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "asc") String sort
    ) {
        PageResponse<RoomResponse> roomPage = roomService.searchRooms(q, minPrice, maxPrice, minArea, page, size, sort);
        return ResponseEntity.ok(roomPage);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy thông tin chi tiết của một phòng")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'OWNER', 'RENTER')")
    public ResponseEntity<BaseResponse<RoomResponse>> getRoomById(@PathVariable Long id) {
        // SỬA LỖI TẠI ĐÂY: Khai báo biến room để nhận kết quả
        RoomResponse room = roomService.getRoomById(id);
        return ResponseEntity.ok(
                BaseResponse.<RoomResponse>builder()
                        .code(HttpStatus.OK.value())
                        .message("Lấy thông tin phòng thành công")
                        .data(room)
                        .build()
        );
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật thông tin của một phòng")
    @PreAuthorize("hasAnyAuthority('OWNER', 'ADMIN') and @securityService.isRoomOwner(#id)")
    public ResponseEntity<BaseResponse<RoomResponse>> updateRoom(@PathVariable Long id, @Valid @RequestBody RoomUpdateRequest request) {
        RoomResponse updatedRoom = roomService.updateRoom(id, request);
        return ResponseEntity.ok(
                BaseResponse.<RoomResponse>builder()
                        .code(HttpStatus.OK.value())
                        .message("Cập nhật thông tin phòng thành công")
                        .data(updatedRoom)
                        .build()
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa một phòng (xóa mềm)")
    @PreAuthorize("hasAnyAuthority('OWNER', 'ADMIN') and @securityService.isRoomOwner(#id)")
    public ResponseEntity<BaseResponse<String>> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.ok(
                BaseResponse.<String>builder()
                        .code(HttpStatus.OK.value())
                        .message("Xóa phòng thành công")
                        .build()
        );
    }
}