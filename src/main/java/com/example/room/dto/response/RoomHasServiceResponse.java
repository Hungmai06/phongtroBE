package com.example.room.dto.response;

import com.example.room.utils.Enums.RoomServiceUsageStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class RoomHasServiceResponse {

    private Long id;

    private Long roomId;
    private String roomName;

    private Long serviceId;
    private String serviceName;

    private BigDecimal pricePerUnit;
    private RoomServiceUsageStatus type;
}