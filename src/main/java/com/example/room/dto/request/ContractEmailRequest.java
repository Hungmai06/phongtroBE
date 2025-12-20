package com.example.room.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractEmailRequest {
    private String recipientName;
    private Long contractId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String roomName;
    private String roomAddress;
    private BigDecimal price;
    private String ownerName;
    private String ownerEmail;
    private String ownerPhone;
    private String renterName;
    private String renterEmail;
    private String renterPhone;
    private String contractUrl;
    private Integer year;
}

