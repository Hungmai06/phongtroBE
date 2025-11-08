package com.example.room.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankAccountResponse {
    private Long id;
    private String bankCode;
    private String bankName;
    private String accountNumber;
    private String accountName;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
