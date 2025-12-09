package com.example.room.dto.response;


import com.example.room.utils.Enums.OwnerRequestStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OwnerRequestResponse {

    private Long id;

    private Long userId;
    private String userName;
    private String phoneNumber;
    private String email;
    private String citizenId;

    private String reason;
    private OwnerRequestStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}