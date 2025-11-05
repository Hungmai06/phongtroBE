package com.example.room.mapper;

import com.example.room.dto.response.RoomServiceResponse;
import com.example.room.model.RoomService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoomServiceMapper {

    @Mapping(target = "roomId", source = "room.id")
    RoomServiceResponse toResponse(RoomService entity);
}