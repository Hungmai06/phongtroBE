package com.example.room.dto.response;

import com.example.room.utils.Enums.ContactStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Builder
@Getter
public class ContactMessageResponse {

    private Long id;
    private String fullName;
    private String phone;
    private String email;
    private String subject;
    private String message;
    private LocalDateTime createdAt;
    private ContactStatus status;
}