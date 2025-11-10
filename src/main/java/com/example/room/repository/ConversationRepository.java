package com.example.room.repository;

import com.example.room.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByUser1IdAndUser2Id(Long user1Id, Long user2Id);

    Optional<Conversation> findByUser2IdAndUser1Id(Long user2Id, Long user1Id);

    @Query("SELECT c FROM Conversation c WHERE c.user1.id = :userId OR c.user2.id = :userId ORDER BY c.id DESC")
    List<Conversation> findAllByUser(@Param("userId") Long userId);
}