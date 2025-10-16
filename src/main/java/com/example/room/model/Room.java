package com.example.room.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;
import com.example.room.utils.Enums.RoomStatus;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;
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

    @Column(name = "utilities")
    private String utilities;// tiện ích

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private RoomStatus status;

    @ManyToOne
    @JoinColumn(name = "owner_id",nullable = false)
    private User owner;

    @OneToMany(mappedBy = "room")
    private List<Booking> bookings;

}
