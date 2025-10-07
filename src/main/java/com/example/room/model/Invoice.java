package com.example.room.model;

import com.example.room.utils.Enums.InvoiceStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "invoice")
public class Invoice extends BaseEntity{
    @Column(name = "invoice_number",nullable = false)
    private String invoiceNumber;

    @Column(name = "issued_date",nullable = false)
    private LocalDateTime issuedDate;

    @Column(name = "file_url")
    private String fileUrl;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "status")
    private InvoiceStatus status;

    @OneToMany(mappedBy = "invoice")
    private List<Payment> payments;

    @OneToMany(mappedBy = "invoice")
    private List<InvoiceDetails> invoiceDetails;
}
