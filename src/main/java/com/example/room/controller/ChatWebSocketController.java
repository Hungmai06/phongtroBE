package com.example.room.controller;

import com.example.room.dto.request.SendMessageRequest;
import com.example.room.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    @MessageMapping("/chat.send")
    public void send(SendMessageRequest payload) {
        var response = chatService.sendMessage(payload);

        Long receiverId = payload.getReceiverId();

        // Gửi đến topic riêng cho user nhận
        messagingTemplate.convertAndSend(
                "/topic/user." + receiverId,
                response.getData()
        );

        System.out.println("Sent to /topic/user." + receiverId);
    }
}