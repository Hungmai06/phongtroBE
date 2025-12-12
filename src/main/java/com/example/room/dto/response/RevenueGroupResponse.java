package com.example.room.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevenueGroupResponse {
    private String period; // e.g. 2025-11
    private BigDecimal totalAmount;
}

