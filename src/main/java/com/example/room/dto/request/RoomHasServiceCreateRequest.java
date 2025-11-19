package com.example.room.dto.request;

import lombok.Data;

@Data
public class RoomHasServiceCreateRequest {
    private Long roomId;
    private Long serviceId;
}