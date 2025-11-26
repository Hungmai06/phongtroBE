package com.example.room.mapper;

import com.example.room.dto.response.RoomResponse;
import com.example.room.model.Room;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface RoomMapper {
    @Mapping(source = "owner.fullName", target = "ownerName")
    RoomResponse toResponse(Room room);

}