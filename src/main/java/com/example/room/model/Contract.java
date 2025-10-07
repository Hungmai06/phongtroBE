package com.example.room.model;

import com.example.room.utils.Enums.ContractStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "contract")
public class Contract extends BaseEntity{
    @Column(name = "start_date",nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "contract_file")
    private String contractFile;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "status")
    private ContractStatus status;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;
}
