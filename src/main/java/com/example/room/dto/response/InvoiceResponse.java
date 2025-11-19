package com.example.room.dto.response;

import com.example.room.utils.Enums.InvoiceStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class InvoiceResponse {
    private Long id;
    private String invoiceNumber;
    private LocalDateTime issueDate;
    private BigDecimal totalAmount;
    private InvoiceStatus status;
    private PaymentResponse payment;
    private ContractResponse contract;
    private UserResponse user;
    
}