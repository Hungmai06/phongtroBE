package com.example.room.mapper;

import com.example.room.dto.response.UserResponse;
import com.example.room.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(source = "role.name", target = "roleName")
    UserResponse toResponse(User user);
}
