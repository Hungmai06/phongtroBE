package com.example.room.mapper;

import com.example.room.dto.response.RoomServiceUsageResponse;
import com.example.room.model.RoomServiceUsage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface RoomServiceUsageMapper {

    RoomServiceUsageMapper INSTANCE = Mappers.getMapper(RoomServiceUsageMapper.class);

    @Mapping(source = "room.id", target = "roomId")
    @Mapping(source = "room.name", target = "roomName")
    @Mapping(source = "roomService.id", target = "roomServiceId")
    @Mapping(source = "roomService.name", target = "roomServiceName")
    RoomServiceUsageResponse toResponse(RoomServiceUsage entity);
}