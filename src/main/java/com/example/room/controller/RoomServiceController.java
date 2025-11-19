package com.example.room.controller;

import com.example.room.dto.BaseResponse;
import com.example.room.dto.PageResponse;
import com.example.room.dto.request.RoomHasServiceCreateRequest;
import com.example.room.dto.request.RoomServiceCreateRequest;
import com.example.room.dto.response.RoomHasServiceResponse;
import com.example.room.dto.response.RoomServiceResponse;
import com.example.room.service.RoomHasServiceService;
import com.example.room.service.RoomServiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/room-services")
@RequiredArgsConstructor
@Tag(name = " API Room Service", description = "Quản lý danh mục dịch vụ (master data) như điện, nước, wifi, ...")
public class RoomServiceController {

    private final RoomServiceService roomServiceService;
    private final RoomHasServiceService roomHasServiceService;

    @Operation(summary = "Tạo mới dịch vụ")
    @PostMapping("")
    public BaseResponse<RoomServiceResponse> create(
            @RequestBody RoomServiceCreateRequest request) {
        return roomServiceService.create(request);
    }

    @Operation(summary = "Cập nhật dịch vụ")
    @PutMapping("/{id}")
    public BaseResponse<RoomServiceResponse> update(
            @PathVariable Long id,
            @RequestBody RoomServiceCreateRequest request) {
        return roomServiceService.update(id, request);
    }

    @Operation(summary = "Xóa dịch vụ")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        roomServiceService.delete(id);
    }

    @Operation(summary = "Lấy thông tin dịch vụ theo id")
    @GetMapping("/{id}")
    public BaseResponse<RoomServiceResponse> findById(@PathVariable Long id) {
        return roomServiceService.findById(id);
    }

    @Operation(summary = "Danh sách dịch vụ (phân trang)")
    @GetMapping("")
    public PageResponse<RoomServiceResponse> findAll(
            @RequestParam Integer page,
            @RequestParam Integer size) {
        return roomServiceService.findAll(page, size);
    }

    @Operation(summary = "Gán dịch vụ cho phòng (nếu dịch vụ chưa có sẽ tạo mới rồi gán)")
    @PostMapping("/assign")
    public BaseResponse<RoomHasServiceResponse> assignService(
            @RequestBody RoomHasServiceCreateRequest req) {
        return roomHasServiceService.assignServiceToRoom(req);
    }

    @Operation(summary = "Lấy danh sách dịch vụ của một phòng theo roomId")
    @GetMapping("/assign/room/{roomId}")
    public BaseResponse<List<RoomHasServiceResponse>> getServicesByRoom(
            @PathVariable Long roomId) {
        return roomHasServiceService.getServicesByRoom(roomId);
    }

    @Operation(summary = "Gỡ dịch vụ khỏi phòng (xóa liên kết)")
    @DeleteMapping("/assign/{id}")
    public BaseResponse<String> removeServiceFromRoom(@PathVariable Long id) {
        return roomHasServiceService.removeServiceFromRoom(id);
    }
}
