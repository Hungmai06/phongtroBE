package com.example.room.model;

import com.example.room.utils.Enums.ContractStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.util.List;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Where(clause = "deleted = false")
@Table(name = "contract")
public class Contract extends BaseEntity{
    @Column(name = "start_date",nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "contract_file")
    private String contractFile;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "status")
    private ContractStatus status;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @OneToMany(mappedBy = "contract")
    private List<Payment> payments;
}