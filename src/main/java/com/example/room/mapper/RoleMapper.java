package com.example.room.mapper;

import com.example.room.dto.response.RoleResponse;
import com.example.room.model.Role;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    RoleResponse toResponse(Role role);
}
