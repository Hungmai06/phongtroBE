package com.example.room.dto.request;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationRequest {
    private Long user1Id;
    private Long user2Id;
}