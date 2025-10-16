package com.example.room.controller;

import com.example.room.dto.BaseResponse;
import com.example.room.dto.PageResponse;
import com.example.room.dto.request.PaymentCreateRequest;
import com.example.room.dto.request.PaymentUpdateRequest;
import com.example.room.dto.response.PaymentResponse;
import com.example.room.service.PaymentService;
import com.example.room.utils.Enums.ContractStatus;
import com.example.room.utils.Enums.PaymentType;
import com.example.room.repository.ContractRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;


import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
@Tag(name = "API PAYMENT", description = "API cho quản lý thanh toán")
public class PaymentController {

    private final PaymentService paymentService;
    private final ContractRepository contractRepository;

    @PostMapping("")
    @PreAuthorize("hasAuthority('OWNER') and @securityService.canAccessBooking(#request.bookingId)")
    @Operation(summary = "Tạo một khoản thanh toán mới (Owner)")
    public ResponseEntity<BaseResponse<PaymentResponse>> createPayment(
            @Valid @RequestBody PaymentCreateRequest request) {

        // ✅ Validation: Chặn tạo payment MONTHLY nếu booking chưa có hợp đồng ACTIVE
        if (request.getPaymentType() == PaymentType.MONTHLY) {
            boolean noActiveContract = contractRepository
                    .findByBookingIdAndStatus(request.getBookingId(), ContractStatus.ACTIVE)
                    .isEmpty();

            if (noActiveContract) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        BaseResponse.<PaymentResponse>builder()
                                .code(HttpStatus.BAD_REQUEST.value())
                                .message("Không thể tạo thanh toán MONTHLY vì booking này chưa có hợp đồng ACTIVE.")
                                .build()
                );
            }
        }


    // ✅ Nếu hợp lệ → gọi service để tạo payment
    PaymentResponse newPayment = paymentService.createPayment(request);

    return ResponseEntity.status(HttpStatus.CREATED).body(
            BaseResponse.<PaymentResponse>builder()
                    .code(HttpStatus.CREATED.value())
                    .message("Tạo thanh toán thành công")
                    .data(newPayment)
                    .build()
    );
}
    @GetMapping("")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'OWNER', 'RENTER')")
    @Operation(summary = "Lấy danh sách thanh toán")
    public ResponseEntity<PageResponse<PaymentResponse>> getAllPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<PaymentResponse> paymentPage = paymentService.getAllPayments(page, size);
        return ResponseEntity.ok(paymentPage);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'OWNER', 'RENTER') and @securityService.canAccessPayment(#id)")
    @Operation(summary = "Lấy chi tiết một khoản thanh toán")
    public ResponseEntity<BaseResponse<PaymentResponse>> getPaymentById(@PathVariable Long id) {
        PaymentResponse payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(
                BaseResponse.<PaymentResponse>builder()
                        .code(HttpStatus.OK.value())
                        .message("Lấy chi tiết thanh toán thành công")
                        .data(payment)
                        .build()
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'OWNER') and @securityService.canAccessPayment(#id)")
    @Operation(summary = "Cập nhật trạng thái thanh toán (Owner xác nhận đã nhận tiền)")
    public ResponseEntity<BaseResponse<PaymentResponse>> updatePaymentStatus(@PathVariable Long id, @Valid @RequestBody PaymentUpdateRequest request) {
        PaymentResponse updatedPayment = paymentService.updatePaymentStatus(id, request);
        return ResponseEntity.ok(
                BaseResponse.<PaymentResponse>builder()
                        .code(HttpStatus.OK.value())
                        .message("Cập nhật trạng thái thanh toán thành công")
                        .data(updatedPayment)
                        .build()
        );
    }
    
    @PostMapping("/generate-monthly")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<BaseResponse<String>> generateMonthlyPayments() {
        paymentService.generateMonthlyPayments();
        return ResponseEntity.ok(
                BaseResponse.<String>builder()
                        .code(HttpStatus.OK.value())
                        .message("Đã bắt đầu quá trình tạo thanh toán hàng tháng")
                        .build()
        );
    }
}