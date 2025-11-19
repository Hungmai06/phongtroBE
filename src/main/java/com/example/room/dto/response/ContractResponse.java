package com.example.room.dto.response;

import com.example.room.utils.Enums.ContractStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ContractResponse {
    private long id;
    private long bookingId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String contractFile;
    private ContractStatus status;
}