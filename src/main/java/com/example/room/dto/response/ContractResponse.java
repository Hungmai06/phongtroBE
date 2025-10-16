package com.example.room.dto.response;

import com.example.room.utils.Enums.ContractStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ContractResponse {
    private long id;
    private long bookingId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String contractFile;
    private ContractStatus status;
}