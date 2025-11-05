package com.example.room.model;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "bank_accounts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankAccount extends BaseEntity{

    @Column(name = "bank_code", length = 50)
    private String bankCode;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "account_number", nullable = false, unique = true)
    private String accountNumber;

    @Column(name = "account_name")
    private String accountName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}