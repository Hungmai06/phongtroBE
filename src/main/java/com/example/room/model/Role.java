package com.example.room.model;

import com.example.room.utils.Enums.RoleEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "roles")
public class Role extends BaseEntity{
    @Enumerated(value = EnumType.STRING)
    @Column(name = "name",nullable = false)
    private RoleEnum name;

    @Column(name = "description")
    private String description;

    @OneToMany(mappedBy = "role")
    private List<User> users;
}
