package com.example.room.service.Impl;

import com.example.room.dto.BaseResponse;
import com.example.room.dto.PageResponse;
import com.example.room.dto.request.ConversationRequest;
import com.example.room.dto.request.MarkSeenRequest;
import com.example.room.dto.request.SendMessageRequest;
import com.example.room.dto.response.ConversationResponse;
import com.example.room.dto.response.MessageResponse;
import com.example.room.mapper.ChatMapper;
import com.example.room.model.Conversation;
import com.example.room.model.Message;
import com.example.room.model.User;
import com.example.room.repository.ConversationRepository;
import com.example.room.repository.MessageRepository;
import com.example.room.repository.UserRepository;
import com.example.room.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChatMapper chatMapper;

    @Override
    public BaseResponse<ConversationResponse> getOrCreateConversation(ConversationRequest request) {
        Conversation conversation = conversationRepository.findByUser1IdAndUser2Id(request.getUser1Id(), request.getUser2Id())
                .or(() -> conversationRepository.findByUser2IdAndUser1Id(request.getUser1Id(), request.getUser2Id()))
                .orElseGet(() -> {
                    Conversation conv = Conversation.builder()
                            .user1(userRepository.getReferenceById(request.getUser1Id()))
                            .user2(userRepository.getReferenceById(request.getUser2Id()))
                            .build();
                    return conversationRepository.save(conv);
                });

        return BaseResponse.<ConversationResponse>builder()
                .code(200)
                .message("Lấy hoặc tạo cuộc trò chuyện thành công")
                .data(chatMapper.toConversationResponse(conversation))
                .build();
    }

    @Override
    @Transactional
    public BaseResponse<MessageResponse> sendMessage(SendMessageRequest request) {
        Conversation conversation = conversationRepository.findByUser1IdAndUser2Id(request.getSenderId(), request.getReceiverId())
                .or(() -> conversationRepository.findByUser2IdAndUser1Id(request.getSenderId(), request.getReceiverId()))
                .orElseGet(() -> {
                    Conversation conv = Conversation.builder()
                            .user1(userRepository.getReferenceById(request.getSenderId()))
                            .user2(userRepository.getReferenceById(request.getReceiverId()))
                            .build();
                    return conversationRepository.save(conv);
                });

        Message message = Message.builder()
                .conversation(conversation)
                .sender(userRepository.getReferenceById(request.getSenderId()))
                .content(request.getContent())
                .seen(false)
                .build();

        Message saved = messageRepository.save(message);

        return BaseResponse.<MessageResponse>builder()
                .code(200)
                .message("Gửi tin nhắn thành công")
                .data(chatMapper.toMessageResponse(saved))
                .build();
    }

    @Override
    public PageResponse<MessageResponse> getMessages(Long conversationId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messages = messageRepository.findByConversationId(conversationId, pageable);

        return PageResponse.<MessageResponse>builder()
                .code(200)
                .message("Lấy danh sách tin nhắn thành công")
                .data(chatMapper.toMessageResponseList(messages.getContent()))
                .pageNumber(messages.getNumber())
                .pageSize(messages.getSize())
                .totalElements(messages.getTotalElements())
                .totalPages(messages.getTotalPages())
                .build();
    }

    @Override
    public BaseResponse<List<ConversationResponse>> getUserConversations() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Conversation> conversations = conversationRepository.findAllByUser(user.getId());
        return BaseResponse.<List<ConversationResponse>>builder()
                .code(200)
                .message("Lấy danh sách cuộc trò chuyện thành công")
                .data(chatMapper.toConversationResponseList(conversations))
                .build();
    }

    @Override
    @Transactional
    public BaseResponse<String> markMessagesAsSeen(MarkSeenRequest request) {
        Page<Message> messages = messageRepository.findByConversationId(request.getConversationId(), PageRequest.of(0, 100));
        messages.forEach(m -> {
            if (!m.getSender().getId().equals(request.getUserId())) {
                m.setSeen(true);
            }
        });
        messageRepository.saveAll(messages);

        return BaseResponse.<String>builder()
                .code(200)
                .message("Đã đánh dấu tin nhắn là đã xem")
                .data("success")
                .build();
    }
}