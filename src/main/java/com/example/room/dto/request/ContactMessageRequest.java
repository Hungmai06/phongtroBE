package com.example.room.dto.request;


import com.example.room.utils.Enums.ContactStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ContactMessageRequest {

    private String fullName;
    private String phone;
    private String email;
    private String subject;
    private String message;
    private ContactStatus status;
}