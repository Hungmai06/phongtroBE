package com.example.room.dto.request;

import com.example.room.utils.Enums.RoomType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

import com.example.room.utils.Enums.RoomStatus;

@Getter
@Setter
public class RoomUpdateRequest {
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal deposit;
    private Float area;
    private Integer capacity;
    private String address;
    private String utilities;
    private RoomType type;
    private RoomStatus status;
    private List<String> facilities;
}