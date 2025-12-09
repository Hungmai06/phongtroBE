package com.example.room.mapper;

import com.example.room.dto.response.RoomResponse;
import com.example.room.model.Room;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RoomMapper {
    @Mapping(source = "owner.fullName", target = "ownerName")
    @Mapping(source = "owner.id", target = "ownerId")
    RoomResponse toResponse(Room room);
    List<RoomResponse> toResponse(List<Room> rooms);
}