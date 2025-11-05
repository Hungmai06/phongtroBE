package com.example.room.dto.response;

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
public class RoomServiceUsageResponse {
    private Long id;
    private String name;
    private RoomServiceUsageStatus type;

    private Integer quantityOld;
    private Integer quantityNew;
    private Integer quantityUsed;

    private BigDecimal pricePerUnit;
    private BigDecimal totalPrice;

    private LocalDateTime month;
    private LocalDateTime usedAt;

    private Long roomId;
    private String roomName;

    private Long roomServiceId;
    private String roomServiceName;
}