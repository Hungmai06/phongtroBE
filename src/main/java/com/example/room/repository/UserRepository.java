package com.example.room.repository;

import com.example.room.model.Role;
import com.example.room.model.User;
import com.example.room.utils.Enums.RoleEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface UserRepository extends JpaRepository<User,Long>, JpaSpecificationExecutor<User> {

   Optional<User> findByEmail(String email);

   @EntityGraph(attributePaths = {"role"})
   Optional<User> findById(Long id);

   Page<User> findAllByRole(Role role, Pageable pageable);
}

