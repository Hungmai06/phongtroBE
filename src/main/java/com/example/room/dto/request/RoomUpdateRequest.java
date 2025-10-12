package com.example.room.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class RoomUpdateRequest {
    private String name;
    private List<String> images;
    private String description;
    private BigDecimal price;
    private BigDecimal deposit;
    private Float area;
    private Integer capacity;
    private String address;
    private String utilities;
    private Boolean status;
}