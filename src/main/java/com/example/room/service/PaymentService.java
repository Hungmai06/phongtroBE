package com.example.room.service;

import com.example.room.dto.PageResponse;
import com.example.room.dto.request.PaymentCreateRequest;
import com.example.room.dto.request.PaymentUpdateRequest;
import com.example.room.dto.response.PaymentResponse;
import com.example.room.utils.Enums.PaymentMethod;
import com.example.room.utils.Enums.PaymentStatus;
import com.example.room.utils.Enums.PaymentType;

import java.time.LocalDateTime;

public interface PaymentService {

    PaymentResponse createPayment(PaymentCreateRequest request);

    PaymentResponse updatePaymentStatus(Long id, PaymentUpdateRequest request);

    PageResponse<PaymentResponse> getAllPayments(
            int page,
            int size,
            Long bookingId,
            PaymentType paymentType,
            PaymentMethod paymentMethod,
            PaymentStatus paymentStatus,
            LocalDateTime paymentDate,
            LocalDateTime createdAt
    );

    PaymentResponse getPaymentById(Long id);

    void generateMonthlyPayments();
}