package com.example.room.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "room_services")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomService extends BaseEntity {

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", precision = 13, scale = 2, nullable = false)
    private BigDecimal price;

    @OneToMany(mappedBy = "roomService", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoomHasService> rooms = new ArrayList<>();
}
