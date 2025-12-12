package com.example.room.service;

import com.example.room.dto.BaseResponse;
import com.example.room.dto.PageResponse;
import com.example.room.dto.request.*;
import com.example.room.dto.response.*;

import java.util.List;

public interface ChatService {

    BaseResponse<ConversationResponse> getOrCreateConversation(ConversationRequest request);

    BaseResponse<MessageResponse> sendMessage(SendMessageRequest request);

    PageResponse<MessageResponse> getMessages(Long conversationId, int page, int size);

    BaseResponse<List<ConversationResponse>> getUserConversations();

    BaseResponse<String> markMessagesAsSeen(MarkSeenRequest request);
}