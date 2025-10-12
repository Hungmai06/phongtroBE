package com.example.room.repository;

import com.example.room.model.Role;
import com.example.room.utils.Enums.RoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role,Long> {
        Optional<Role> findByName(RoleEnum name);

}
