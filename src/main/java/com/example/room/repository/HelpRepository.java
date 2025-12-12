package com.example.room.repository;

import com.example.room.model.Help;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface HelpRepository extends JpaRepository<Help, Long>, JpaSpecificationExecutor<Help> {

}
