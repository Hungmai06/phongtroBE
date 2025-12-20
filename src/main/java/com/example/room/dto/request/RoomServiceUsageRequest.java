package com.example.room.dto.request;

import com.example.room.utils.Enums.RoomServiceUsageStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomServiceUsageRequest {
    private String name;
    private RoomServiceUsageStatus type;

    private Integer quantityOld;
    private Integer quantityNew;

    private String month;

    private Long roomId;
    private Long roomServiceId;
}