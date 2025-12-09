package com.example.room.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RoomTenantResponse {
    private Long roomId;
    private String roomName;
    private String roomAddress;
    private String roomStatus;

    private Long renterId;
    private String renterName;
    private String renterEmail;
    private String renterPhone;

    private Long contractId;
    private LocalDateTime contractStartDate;
    private LocalDateTime contractEndDate;
}