package com.example.room.model;

import com.example.room.utils.Enums.OwnerRequestStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "owner_requests")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OwnerRequest extends BaseEntity{

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @Column(length = 1000)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private OwnerRequestStatus status;

}
