package com.example.room.model;

import com.example.room.utils.Enums.ContactStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "helps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Help extends BaseEntity{

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "subject", nullable = false, length = 200)
    private String subject;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private ContactStatus status;
}
