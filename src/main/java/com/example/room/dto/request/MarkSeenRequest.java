package com.example.room.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarkSeenRequest {
    private Long conversationId;
    private Long userId;
}