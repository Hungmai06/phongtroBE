package com.example.room.mapper;

import com.example.room.dto.response.RoomHasServiceResponse;
import com.example.room.model.RoomHasService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoomHasServiceMapper {

    @Mapping(source = "room.id", target = "roomId")
    @Mapping(source = "room.name", target = "roomName")
    @Mapping(source = "roomService.id", target = "serviceId")
    @Mapping(source = "roomService.name", target = "serviceName")
    RoomHasServiceResponse toResponse(RoomHasService entity);
}