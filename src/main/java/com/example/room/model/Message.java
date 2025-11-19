package com.example.room.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

@Entity
@Table(
        name = "messages",
        indexes = {
                @Index(name = "idx_msg_conv_sentat", columnList = "conversation_id, created_at DESC")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Where(clause = "deleted = false")
public class Message extends BaseEntity{
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="conversation_id")
    private Conversation conversation;

    @ManyToOne
    @JoinColumn(name="sender_id")
    private User sender;

    @Column(nullable=false, columnDefinition="TEXT")
    private String content;

    @Column(nullable = false)
    private Boolean seen = false;
}
