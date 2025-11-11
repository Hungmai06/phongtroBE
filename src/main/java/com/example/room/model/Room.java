package com.example.room.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;
import com.example.room.utils.Enums.RoomStatus;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Where(clause = "deleted = false")
@Table(name = "rooms")
public class Room extends BaseEntity{

    @Column(name = "name",nullable = false)
    private String name;
    
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Image> images = new ArrayList<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoomService> services = new ArrayList<>();

    @Column(name = "description",nullable = false)
    private String description;

    @Column(name = "price",nullable = false,precision = 12 ,scale = 2)
    private BigDecimal price;

    @Column(name = "deposit",nullable = false,precision = 12 ,scale = 2)
    private BigDecimal deposit;

    @Column(name = "area",nullable = false)
    private Float area; // diện tích

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "address",nullable = false)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private RoomStatus status;

    @ManyToOne
    @JoinColumn(name = "owner_id",nullable = false)
    private User owner;

    @OneToMany(mappedBy = "room")
    private List<Booking> bookings;

}
