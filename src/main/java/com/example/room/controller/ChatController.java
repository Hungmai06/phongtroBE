package com.example.room.controller;

import com.example.room.dto.BaseResponse;
import com.example.room.dto.PageResponse;
import com.example.room.dto.request.*;
import com.example.room.dto.response.*;
import com.example.room.service.ChatService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "API CHAT", description = "API cho chức năng trò chuyện")
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/conversation")
    @Operation(summary = "Tạo hoặc lấy conversation", description = "Tạo một cuộc trò chuyện mới hoặc trả về conversation hiện có giữa các user")
    public BaseResponse<ConversationResponse> getOrCreateConversation(@RequestBody ConversationRequest request) {
        return chatService.getOrCreateConversation(request);
    }

    @PostMapping("/send")
    @Operation(summary = "Gửi tin nhắn", description = "Gửi một tin nhắn trong conversation")
    public BaseResponse<MessageResponse> sendMessage(@RequestBody SendMessageRequest request) {
        return chatService.sendMessage(request);
    }

    @GetMapping("/messages")
    @Operation(summary = "Lấy danh sách tin nhắn", description = "Lấy tin nhắn theo conversationId, hỗ trợ phân trang (page, size)")
    public PageResponse<MessageResponse> getMessages(
            @Parameter(description = "ID của conversation", required = true)
            @RequestParam Long conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return chatService.getMessages(conversationId, page, size);
    }

    @GetMapping("/conversations")
    @Operation(summary = "Lấy danh sách conversation của user", description = "Trả về danh sách conversation mà user tham gia")
    public BaseResponse<List<ConversationResponse>> getUserConversations() {
        return chatService.getUserConversations();
    }

    @PostMapping("/seen")
    @Operation(summary = "Đánh dấu tin nhắn đã xem", description = "Đánh dấu các tin nhắn trong conversation là đã được xem bởi user")
    public BaseResponse<String> markMessagesAsSeen(@RequestBody MarkSeenRequest request) {
        return chatService.markMessagesAsSeen(request);
    }
}