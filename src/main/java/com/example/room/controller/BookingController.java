package com.example.room.controller;

import com.example.room.dto.BaseResponse;
import com.example.room.dto.request.BookingCreateRequest;
import com.example.room.dto.request.BookingUpdateRequest;
import com.example.room.dto.response.BookingResponse;
import com.example.room.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
    @PreAuthorize("hasAuthority('RENTER')")
    @Operation(summary = "Tạo đơn đặt phòng (Renter)")
    public ResponseEntity<BaseResponse> createBooking(@Valid @RequestBody BookingCreateRequest request) {
        BookingResponse booking = bookingService.createBooking(request);
        return ResponseEntity.ok(
                BaseResponse.builder()
                        .code(HttpStatus.OK.value())
                        .message("Tạo đơn đặt phòng thành công")
                        .data(booking)
                        .build()
        );
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'OWNER', 'RENTER')")
    @Operation(summary = "Lấy danh sách tất cả booking (Admin xem tất cả, Owner và Renter chỉ xem của mình)")
    public ResponseEntity<BaseResponse> getAllBookings(Pageable pageable) {
        Page<BookingResponse> bookings = bookingService.getAllBookings(pageable);
        return ResponseEntity.ok(
                BaseResponse.builder()
                        .code(HttpStatus.OK.value())
                        .message("Lấy danh sách booking thành công")
                        .data(bookings)
                        .build()
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'OWNER', 'RENTER') and @securityService.canAccessBooking(#id)")
    @Operation(summary = "Lấy chi tiết một booking theo ID")
    public ResponseEntity<BaseResponse> getBookingById(@PathVariable Long id) {
        BookingResponse booking = bookingService.getBookingById(id);
        return ResponseEntity.ok(
                BaseResponse.builder()
                        .code(HttpStatus.OK.value())
                        .message("Lấy chi tiết booking thành công")
                        .data(booking)
                        .build()
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'OWNER') and @securityService.canAccessBooking(#id)")
    @Operation(summary = "Cập nhật trạng thái booking (Owner xác nhận, Admin có quyền chỉnh sửa)")
    public ResponseEntity<BaseResponse> updateBookingStatus(@PathVariable Long id,
                                                            @Valid @RequestBody BookingUpdateRequest request) {
        BookingResponse updated = bookingService.updateBookingStatus(id, request);
        return ResponseEntity.ok(
                BaseResponse.builder()
                        .code(HttpStatus.OK.value())
                        .message("Cập nhật trạng thái booking thành công")
                        .data(updated)
                        .build()
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'RENTER') and @securityService.canAccessBooking(#id)")
    @Operation(summary = "Xóa booking (Renter có thể hủy, Admin có thể xóa bất kỳ booking nào)")
    public ResponseEntity<BaseResponse> deleteBooking(@PathVariable Long id) {
        bookingService.deleteBooking(id);
        return ResponseEntity.ok(
                BaseResponse.builder()
                        .code(HttpStatus.OK.value())
                        .message("Xóa booking thành công")
                        .build()
        );
    }
}
