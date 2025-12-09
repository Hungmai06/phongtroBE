package com.example.room.elasticsearch;

import com.example.room.model.Room;

public class RoomMapper {
    public static RoomDocument toDocument(Room room) {
        return RoomDocument.builder()
                .id(room.getId().toString())
                .name(room.getName())
                .description(room.getDescription())
                .address(room.getAddress())
                .type(room.getType() != null ? room.getType().name() : null)
                .images(room.getImages())
                .price(room.getPrice())
                .area(room.getArea())
                .capacity(room.getCapacity())
                .facilities(room.getFacilities())
                .status(room.getStatus() != null ? room.getStatus().name() : null)
                .ownerName(room.getOwner() != null ? room.getOwner().getFullName() : null)
                .deleted(Boolean.TRUE.equals(room.getDeleted()))
                .build();
    }
}