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

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "rooms")
public class Room extends BaseEntity{

    @Column(name = "name",nullable = false)
    private String name;

    @Column(name = "images",nullable = false)
    private String images;

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

    @Column(name = "status")
    private Boolean status;

    @OneToOne
    @JoinColumn(name = "owner_id",nullable = false)
    private User owner;

    @OneToMany(mappedBy = "room")
    private List<Booking> bookings;
}
