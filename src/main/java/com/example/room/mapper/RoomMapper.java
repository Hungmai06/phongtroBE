package com.example.room.mapper;

import com.example.room.dto.response.RoomResponse;
import com.example.room.model.Image;
import com.example.room.model.Room;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface RoomMapper {
    @Mapping(source = "owner.fullName", target = "ownerName")
    @Mapping(source = "images", target = "images")
    RoomResponse toResponse(Room room);

    default List<String> mapImages(List<Image> images) {
        if (images == null) {
            return null;
        }
        return images.stream()
                     .map(Image::getImageUrl)
                     .collect(Collectors.toList());
    }
}