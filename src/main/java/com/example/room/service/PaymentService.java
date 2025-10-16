package com.example.room.service;

import com.example.room.dto.PageResponse;
import com.example.room.dto.request.PaymentCreateRequest;
import com.example.room.dto.request.PaymentUpdateRequest;
import com.example.room.dto.response.PaymentResponse;

public interface PaymentService {

    // Trả về trực tiếp PaymentResponse
    PaymentResponse createPayment(PaymentCreateRequest request);

    // Trả về trực tiếp PaymentResponse
    PaymentResponse updatePaymentStatus(Long id, PaymentUpdateRequest request);

    // Giữ nguyên PageResponse
    PageResponse<PaymentResponse> getAllPayments(int page, int size);

    // Trả về trực tiếp PaymentResponse
    PaymentResponse getPaymentById(Long id);

    void generateMonthlyPayments();
}