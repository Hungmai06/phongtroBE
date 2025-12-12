package com.example.room.controller;

import com.example.room.dto.BaseResponse;
import com.example.room.dto.PageResponse;
import com.example.room.dto.request.ContactMessageRequest;
import com.example.room.dto.response.ContactMessageResponse;
import com.example.room.service.ContactMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/help")
@RequiredArgsConstructor
@Tag(name = "LIÊN HỆ", description = "API xử lý form liên hệ từ người dùng")
public class ContactMessageController {

    private final ContactMessageService contactMessageService;

    @PostMapping
    @Operation(summary = "Gửi liên hệ", description = "Người dùng gửi form liên hệ")
    public BaseResponse<ContactMessageResponse> createContact(
            @RequestBody ContactMessageRequest request
    ) {
        return contactMessageService.createContact(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Xem chi tiết liên hệ")
    public BaseResponse<ContactMessageResponse> getContactById(
            @PathVariable Long id
    ) {
        return contactMessageService.getContactById(id);
    }

    @GetMapping
    @Operation(summary = "Danh sách liên hệ (search + paging)")
    public PageResponse<ContactMessageResponse> searchContacts(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return contactMessageService.searchContacts(q, page, size);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật liên hệ", description = "Sửa thông tin liên hệ theo ID")
    public BaseResponse<ContactMessageResponse> updateContact(
            @PathVariable Long id,
            @RequestBody ContactMessageRequest request
    ) throws MessagingException {
        return contactMessageService.updateStatus(id, request);
    }
}

