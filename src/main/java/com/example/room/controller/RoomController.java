package com.example.room.controller;

import com.example.room.dto.BaseResponse;
import com.example.room.dto.PageResponse;
import com.example.room.dto.request.RoomCreateRequest;
import com.example.room.dto.request.RoomUpdateRequest;
import com.example.room.dto.response.RoomResponse;
import com.example.room.service.RoomService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rooms")
@Tag(name = "API ROOM", description = "API cho quản lý phòng trọ")
public class RoomController {

    private final RoomService roomService;

    @PostMapping(consumes = "multipart/form-data")
    @Operation(summary = "Đăng một phòng trọ mới (thông tin + nhiều ảnh)")
    @PreAuthorize("hasRole('OWNER')")
    public BaseResponse<RoomResponse> createRoom(
            @Valid @RequestPart("data") String request,
            @RequestPart("files") List<MultipartFile> files
    )  throws Exception{
        ObjectMapper mapper = new ObjectMapper();
        RoomCreateRequest readValue = mapper.readValue(request, RoomCreateRequest.class);
        return roomService.createRoom(readValue, files);
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
       return  roomService.getRoomById(id);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Cập nhật thông tin phòng + ảnh")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN') and @securityService.isRoomOwner(#id)")
    public BaseResponse<RoomResponse> updateRoom(
            @PathVariable Long id,
            @Valid @RequestPart("data") String request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    )  throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        RoomUpdateRequest readValue = mapper.readValue(request, RoomUpdateRequest.class);
        return roomService.updateRoom(id, readValue, files);
    }


    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa một phòng (xóa mềm)")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN') and @securityService.isRoomOwner(#id)")
    public void deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
    }
}