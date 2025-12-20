package com.example.room.dto.request;

import com.example.room.utils.Enums.RoomServiceUsageStatus;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RoomHasServiceCreateRequest {
    private Long roomId;
    private Long serviceId;
    private BigDecimal pricePerUnit;
    private RoomServiceUsageStatus type;
}