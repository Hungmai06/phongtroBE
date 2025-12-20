package com.example.room.dto.request;

import com.example.room.utils.Enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PaymentUpdateRequest {

    @NotNull(message = "Trạng thái thanh toán không được để trống")
    private PaymentStatus paymentStatus;

    private LocalDate startDate;
    private LocalDate endDate;

}