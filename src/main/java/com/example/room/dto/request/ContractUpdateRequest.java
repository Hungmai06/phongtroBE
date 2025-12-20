package com.example.room.dto.request;

import com.example.room.utils.Enums.ContractStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ContractUpdateRequest {
    private LocalDate endDate; // Dùng để gia hạn
    private ContractStatus status; // Dùng để chấm dứt (TERMINATED)
}