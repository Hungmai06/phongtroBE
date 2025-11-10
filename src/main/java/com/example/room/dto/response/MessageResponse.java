package com.example.room.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageResponse {
    private Long id;
    private String content;
    private Long senderId;
    private String senderName;
    private LocalDateTime createdAt;
    private Boolean seen;
}