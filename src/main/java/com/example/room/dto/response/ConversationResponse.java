package com.example.room.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationResponse {
    private Long id;
    private Long user1Id;
    private String user1Name;
    private Long user2Id;
    private String user2Name;
}