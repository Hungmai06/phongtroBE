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

@Getter
@Setter
@Builder
public class PaymentCreateRequest {

    @NotNull(message = "Booking ID không được để trống")
    private Long bookingId;

    @NotNull(message = "Loại thanh toán không được để trống")
    private PaymentType paymentType;

    @NotNull(message = "Số tiền không được để trống")
    @DecimalMin(value = "0.0", message = "Số tiền phải lớn hơn hoặc bằng 0")
    private BigDecimal amount;

    private String description;

    private PaymentMethod paymentMethod;

}