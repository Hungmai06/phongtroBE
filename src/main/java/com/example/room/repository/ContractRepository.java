package com.example.room.repository;

import com.example.room.model.Contract;
import com.example.room.utils.Enums.ContractStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {
    List<Contract> findByStatus(ContractStatus status);
    List<Contract> findByBookingIdAndStatus(Long bookingId, ContractStatus status);
    Page<Contract> findByBooking_User_Id(Long userId, Pageable pageable);
    Page<Contract> findByBooking_Room_Owner_Id(Long ownerId, Pageable pageable);
    List<Contract> findByBooking_Id(Long bookingId);
}