package com.example.room.repository;

import com.example.room.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long>, JpaSpecificationExecutor<Room> {
    // Lấy danh sách phòng theo owner id (field trong Room là `owner`)
    List<Room> findAllByOwnerId(Long ownerId);
}