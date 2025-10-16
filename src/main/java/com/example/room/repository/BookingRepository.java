package com.example.room.repository;

import com.example.room.model.Booking;
import com.example.room.utils.Enums.BookingStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Page<Booking> findByUser_Id(Long userId, Pageable pageable);
    Page<Booking> findByRoom_Owner_Id(Long ownerId, Pageable pageable);
    List<Booking> findByStatusAndExpirationDateBefore(BookingStatus status, LocalDateTime now);
}