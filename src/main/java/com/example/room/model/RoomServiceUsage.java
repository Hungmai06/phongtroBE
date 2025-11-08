package com.example.room.model;

import com.example.room.utils.Enums.RoomServiceUsageStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "room_service_usages")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomServiceUsage extends BaseEntity {

    @Column(name = "name", length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 100)
    private RoomServiceUsageStatus type;

    @Column(name = "quantity_old")
    private Integer quantityOld;

    @Column(name = "month", nullable = false)
    private LocalDate month;

    @Column(name = "quantity_new")
    private Integer quantityNew;

    @Column(name = "quantity_used", nullable = false)
    private Integer quantityUsed = 1;

    @Column(name = "price_per_unit", precision = 13, scale = 2, nullable = false)
    private BigDecimal pricePerUnit;

    @Column(name = "total_price", precision = 13, scale = 2, nullable = false)
    private BigDecimal totalPrice;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_service_id")
    private RoomService roomService;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;
}
