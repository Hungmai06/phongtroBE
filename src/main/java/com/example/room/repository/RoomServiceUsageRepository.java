package com.example.room.repository;

import com.example.room.model.RoomServiceUsage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface RoomServiceUsageRepository extends JpaRepository<RoomServiceUsage,Long> {
    Page<RoomServiceUsage> findByMonth(LocalDateTime month, Pageable pageable);

    List<RoomServiceUsage> findByRoomIdAndMonth(Long roomId, LocalDate month );
}
