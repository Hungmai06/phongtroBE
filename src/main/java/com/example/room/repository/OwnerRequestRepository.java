package com.example.room.repository;

import com.example.room.model.OwnerRequest;
import com.example.room.utils.Enums.OwnerRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OwnerRequestRepository extends JpaRepository<OwnerRequest, Long> {
    boolean existsByUser_IdAndStatus(Long userId, OwnerRequestStatus status);
    Page<OwnerRequest> findByStatus(OwnerRequestStatus status, Pageable pageable);
}
