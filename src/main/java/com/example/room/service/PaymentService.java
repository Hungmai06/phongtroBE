package com.example.room.service;

import com.example.room.dto.BaseResponse;
import com.example.room.dto.PageResponse;
import com.example.room.dto.request.PaymentCreateRequest;
import com.example.room.dto.request.PaymentUpdateRequest;
import com.example.room.dto.response.OwnerRevenuePeriodDto;
import com.example.room.dto.response.PaymentResponse;
import com.example.room.dto.response.RevenueByOwnerResponse;
import com.example.room.utils.Enums.PaymentMethod;
import com.example.room.utils.Enums.PaymentStatus;
import com.example.room.utils.Enums.PaymentType;
import jakarta.mail.MessagingException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface PaymentService {

    BaseResponse<PaymentResponse> createPayment(PaymentCreateRequest request) throws MessagingException;

    BaseResponse<PaymentResponse> updatePaymentStatus(Long id, PaymentUpdateRequest request);

    PageResponse<PaymentResponse> getAllPayments(
            int page,
            int size,
            Long bookingId,
            PaymentType paymentType,
            PaymentMethod paymentMethod,
            PaymentStatus paymentStatus,
            LocalDateTime paymentDate,
            LocalDate paymentPeriod
    );

    BaseResponse<PaymentResponse> getPaymentById(Long id);
    BaseResponse<PaymentResponse> getPaymentByRoomId(Long roomId);
    BaseResponse<BigDecimal> ownerRevenueInMonth(Long ownerId, String period);
    void deletePayment(Long id);
}