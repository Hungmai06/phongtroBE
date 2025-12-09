package com.example.room.dto.response;

import com.example.room.utils.Enums.RoomType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import com.example.room.utils.Enums.RoomStatus;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
public class RoomResponse {
    private Long id;
    private String name;
    private List<String> images;
    private List<String> facilities;
    private String description;
    private BigDecimal price;
    private BigDecimal deposit;
    private Float area;
    private Integer capacity;
    private String address;
    private RoomStatus status;
    private RoomType type;
    private String ownerName;
    private Long ownerId;
}