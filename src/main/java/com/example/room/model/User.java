package com.example.room.model;

import com.example.room.utils.Enums.GenderEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
@Where(clause = "deleted = false")
public class User extends BaseEntity{

    @Column(unique = true, nullable = false,length = 128)
    private String email;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "phone" ,length = 15)
    private String phone;

    @Column(nullable = false, length = 64)
    private String password;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "gender")
    private GenderEnum gender;

    @Column(name = "address")
    private String address;

    @Column(name = "citizen_id")
    private String citizenId;

    @ManyToOne( fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;

    @OneToOne(mappedBy = "owner")
    private Room room;

    @OneToMany(mappedBy = "user")
    private List<Booking> bookings;
}