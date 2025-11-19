package com.example.room.dto.response;

import lombok.*;

import java.math.BigDecimal;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomServiceResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
}
