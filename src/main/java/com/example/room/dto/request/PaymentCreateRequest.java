package com.example.room.dto.request;

import com.example.room.utils.Enums.PaymentMethod;
import com.example.room.utils.Enums.PaymentStatus;
import com.example.room.utils.Enums.PaymentType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Getter
@Setter
@Builder
public class PaymentCreateRequest {
    private Long roomId;

    private Long bookingId;

    private String paymentPeriod;

    private String description;

    @NotNull(message = "Loại thanh toán không được để trống")
    private PaymentType paymentType;

    private PaymentMethod paymentMethod;

}