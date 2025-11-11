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

@Getter
@Setter
@Builder
public class PaymentCreateRequest {

    private Long bookingId;

    private LocalDate paymentPeriod;

    private String description;

    @NotNull(message = "Loại thanh toán không được để trống")
    private PaymentType paymentType;

    @NotNull(message = "Số tiền không được để trống")
    @DecimalMin(value = "0.0", message = "Số tiền phải lớn hơn hoặc bằng 0")
    private BigDecimal amount;

    private PaymentMethod paymentMethod;

}