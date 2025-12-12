package com.example.room.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@Setter
@Builder
@NoArgsConstructor
public class OwnerRevenuePeriodDto {
    private Long ownerId;
    private String ownerName;
    private String ownerEmail;
    private BigDecimal totalRevenue;
    private Long totalPayments;
}