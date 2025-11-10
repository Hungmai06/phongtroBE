package com.example.room.mapper;

import com.example.room.dto.response.*;
import com.example.room.model.*;
import org.mapstruct.*;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ChatMapper {

    // === Message mapping ===
    @Mapping(source = "sender.id", target = "senderId")
    @Mapping(source = "sender.fullName", target = "senderName")
    MessageResponse toMessageResponse(Message message);

    List<MessageResponse> toMessageResponseList(List<Message> messages);

    // === Conversation mapping ===
    @Mapping(source = "user1.id", target = "user1Id")
    @Mapping(source = "user1.fullName", target = "user1Name")
    @Mapping(source = "user2.id", target = "user2Id")
    @Mapping(source = "user2.fullName", target = "user2Name")
    ConversationResponse toConversationResponse(Conversation conversation);

    List<ConversationResponse> toConversationResponseList(List<Conversation> conversations);
}
