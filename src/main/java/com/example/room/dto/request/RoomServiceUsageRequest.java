package com.example.room.dto.request;

import com.example.room.utils.Enums.RoomServiceUsageStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomServiceUsageRequest {
    private String name;
    private RoomServiceUsageStatus type;

    private Integer quantityOld;
    private Integer quantityNew;

    private BigDecimal pricePerUnit;

    private LocalDateTime month;
    private LocalDateTime usedAt;

    private Long roomId;
    private Long roomServiceId;
}