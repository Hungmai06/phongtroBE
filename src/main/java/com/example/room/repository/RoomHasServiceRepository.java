package com.example.room.repository;


import com.example.room.model.RoomHasService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomHasServiceRepository extends JpaRepository<RoomHasService, Long> {

    List<RoomHasService> findByRoomId(Long roomId);
    Optional<RoomHasService> findByRoomIdAndRoomServiceId(Long roomId, Long roomServiceId);
}