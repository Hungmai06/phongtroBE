package com.example.room.controller;

import com.example.room.dto.BaseResponse;
import com.example.room.dto.PageResponse;
import com.example.room.dto.request.PaymentCreateRequest;
import com.example.room.dto.request.PaymentUpdateRequest;
import com.example.room.dto.response.PaymentResponse;
import com.example.room.repository.ContractRepository;
import com.example.room.service.PaymentService;
import com.example.room.utils.Enums.PaymentMethod;
import com.example.room.utils.Enums.PaymentStatus;
import com.example.room.utils.Enums.PaymentType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
@Tag(name = "API PAYMENT", description = "API cho quản lý thanh toán")
public class PaymentController{

    private final PaymentService paymentService;
    private final ContractRepository contractRepository;

    @PostMapping("")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Tạo một khoản thanh toán mới (Owner)")
    public BaseResponse<PaymentResponse> createPayment(
            @Valid @RequestBody PaymentCreateRequest request) throws MessagingException {
        return paymentService.createPayment(request);
    }

    @GetMapping("")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'RENTER')")
    @Operation(summary = "Lấy danh sách thanh toán")
    public PageResponse<PaymentResponse> getAllPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long bookingId,
            @RequestParam(required = false) PaymentType paymentType,
            @RequestParam(required = false) PaymentMethod paymentMethod,
            @RequestParam(required = false) PaymentStatus paymentStatus,
            @RequestParam(required = false) LocalDateTime paymentDate,
            @RequestParam(required = false) LocalDate paymentPeriod
            ) {
        return paymentService.getAllPayments(page, size, bookingId, paymentType, paymentMethod, paymentStatus, paymentDate, paymentPeriod);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'RENTER')")
    @Operation(summary = "Lấy chi tiết một khoản thanh toán")
    public BaseResponse<PaymentResponse> getPaymentById(@PathVariable Long id) {
       return  paymentService.getPaymentById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    @Operation(summary = "Cập nhật trạng thái thanh toán (Owner xác nhận đã nhận tiền)")
    public BaseResponse<PaymentResponse> updatePaymentStatus(@PathVariable Long id, @Valid @RequestBody PaymentUpdateRequest request) {
        return paymentService.updatePaymentStatus(id, request);
    }

    @GetMapping("/room/{roomId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'RENTER')")
    @Operation(summary = "Lấy khoản thanh toán theo Room ID")
    public BaseResponse<PaymentResponse> getPaymentByRoomId(@PathVariable Long roomId) {
        return paymentService.getPaymentByRoomId(roomId);
    }

    @GetMapping("/revenue/owner/month")
    public BaseResponse<BigDecimal> ownerRevenueInMonth(
            @RequestParam Long ownerId,
            @RequestParam String period   // "YYYY-MM"
    ) {
        return paymentService.ownerRevenueInMonth(ownerId, period);
    }
}