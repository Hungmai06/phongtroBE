package com.example.room.repository;

import com.example.room.model.User;
import com.example.room.utils.Enums.RoleEnum;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface UserRepository extends JpaRepository<User,Long>, JpaSpecificationExecutor<User> {

   Optional<User> findByEmail(String email);

   @EntityGraph(attributePaths = {"role"})
   Optional<User> findById(Long id);

   @Query("SELECT u FROM User u WHERE u.role.id = :id")
   Optional<User> findByRoleId(@Param("id") Long id);
}

