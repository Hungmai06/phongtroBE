package com.example.room.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "conversations",
        indexes = {
                @Index(name = "idx_conv_user1", columnList = "user1_id"),
                @Index(name = "idx_conv_user2", columnList = "user2_id")
        }
)
public class Conversation {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="user1_id")
    private User user1;

    @ManyToOne @JoinColumn(name="user2_id")
    private User user2;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL)
    private List<Message> messages = new ArrayList<>();
}
