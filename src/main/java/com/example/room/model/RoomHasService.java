package com.example.room.model;

import com.example.room.utils.Enums.RoomServiceUsageStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;

@Entity
@Table(name = "room_has_services")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Where(clause = "deleted = false")
public class RoomHasService extends BaseEntity {

    @Column(name = "price_per_unit", precision = 13, scale = 2, nullable = false)
    private BigDecimal pricePerUnit;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 100)
    private RoomServiceUsageStatus type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private RoomService roomService;

}
