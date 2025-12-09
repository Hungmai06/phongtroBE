package com.example.room.dto.request;

import com.example.room.model.Room;
import jakarta.annotation.Nonnull;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;

import java.math.BigDecimal;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomServiceCreateRequest {
    private String name;
    private String description;
}
