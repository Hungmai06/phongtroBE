package com.example.room.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "invoice_details")
public class InvoiceDetails extends BaseEntity{
    @Column(name = "item_name",nullable = false)
    private String itemName;

    @Column(name = "description")
    private String description;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "unit_price",precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "sub_total",precision = 12, scale = 2)
    private BigDecimal subTotal;

    @ManyToOne
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;
}
