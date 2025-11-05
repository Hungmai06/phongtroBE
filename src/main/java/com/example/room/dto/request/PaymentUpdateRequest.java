package com.example.room.dto.request;

import com.example.room.utils.Enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentUpdateRequest {

    @NotNull(message = "Trạng thái thanh toán không được để trống")
    private PaymentStatus paymentStatus;

}