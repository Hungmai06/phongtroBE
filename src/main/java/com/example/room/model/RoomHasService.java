package com.example.room.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "room_has_services")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomHasService extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private RoomService roomService;

}
