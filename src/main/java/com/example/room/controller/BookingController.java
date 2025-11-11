package com.example.room.controller;

import com.example.room.dto.BaseResponse;
import com.example.room.dto.PageResponse;
import com.example.room.dto.request.BookingCreateRequest;
import com.example.room.dto.request.BookingUpdateRequest;
import com.example.room.dto.response.BookingResponse;
import com.example.room.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Tag(name = "API BOOKING", description = "API quản lý đặt phòng (Booking)")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @PreAuthorize("hasRole('RENTER')")
    @Operation(summary = "Tạo đơn đặt phòng (Renter)")
    public BaseResponse<BookingResponse> createBooking(@Valid @RequestBody BookingCreateRequest request) throws MessagingException {
      return bookingService.createBooking(request);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'RENTER')")
    @Operation(summary = "Lấy danh sách tất cả booking (Admin xem tất cả, Owner và Renter chỉ xem của mình)")
    public PageResponse<BookingResponse> getAllBookings(@RequestParam Integer page,@RequestParam Integer size) {
        return bookingService.getAllBookings(page, size);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'RENTER') and @securityService.canAccessBooking(#id)")
    @Operation(summary = "Lấy chi tiết một booking theo ID")
    public BaseResponse<BookingResponse> getBookingById(@PathVariable Long id) {
       return bookingService.getBookingById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER') and @securityService.canAccessBooking(#id)")
    @Operation(summary = "Cập nhật trạng thái booking (Owner xác nhận, Admin có quyền chỉnh sửa)")
    public BaseResponse<BookingResponse> updateBookingStatus(@PathVariable Long id,
                                                            @Valid @RequestBody BookingUpdateRequest request) throws MessagingException {
        return bookingService.updateBookingStatus(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RENTER') and @securityService.canAccessBooking(#id)")
    @Operation(summary = "Xóa booking (Renter có thể hủy, Admin có thể xóa bất kỳ booking nào)")
    public BaseResponse<?> deleteBooking(@PathVariable Long id) {
        bookingService.deleteBooking(id);
        return BaseResponse.builder()
                        .code(204)
                        .message("Xóa booking thành công")
                        .build();
    }
}
