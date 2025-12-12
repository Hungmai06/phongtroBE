package com.example.room.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
public class RevenueByOwnerResponse {
    private String fromPeriod;
    private String toPeriod;
    private BigDecimal grandTotal;
    private List<OwnerRevenuePeriodDto> owners;
}