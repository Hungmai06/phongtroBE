package com.example.room.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
public class RoomResponse {
    private Long id;
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
    private String ownerName;
}