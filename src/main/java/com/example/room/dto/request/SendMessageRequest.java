package com.example.room.dto.request;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendMessageRequest {
    private Long senderId;
    private Long receiverId;
    private String content;
}