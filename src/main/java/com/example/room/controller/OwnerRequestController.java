package com.example.room.controller;

import com.example.room.dto.BaseResponse;
import com.example.room.dto.PageResponse;
import com.example.room.dto.request.OwnerRequestRequest;
import com.example.room.dto.request.OwnerRequestUpdate;
import com.example.room.dto.response.OwnerRequestResponse;
import com.example.room.service.OwnerRequestService;
import com.example.room.utils.Enums.OwnerRequestStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/owner-requests")
@RequiredArgsConstructor
@Tag(name = "Owner Request API", description = "API quản lý yêu cầu làm chủ nhà")
public class OwnerRequestController {

    private final OwnerRequestService ownerRequestService;

    @Operation(summary = "Tạo yêu cầu làm chủ nhà")
    @PostMapping("")
    public BaseResponse<OwnerRequestResponse> createRequest(
            @RequestBody OwnerRequestRequest request) {
        return ownerRequestService.createRequest(request);
    }

    @Operation(summary = "Xử lý (phê duyệt/từ chối) yêu cầu chủ nhà")
    @PutMapping("/handle")
    public BaseResponse<OwnerRequestResponse> handleRequest(
            @RequestBody OwnerRequestUpdate update) throws MessagingException {
        return ownerRequestService.handle(update);
    }

    @Operation(summary = "Lấy danh sách yêu cầu chủ nhà (phân trang, theo trạng thái)")
    @GetMapping("")
    public PageResponse<OwnerRequestResponse> getRequests(
            @RequestParam(required = false) OwnerRequestStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ownerRequestService.getRequests(status, page, size);
    }
}
