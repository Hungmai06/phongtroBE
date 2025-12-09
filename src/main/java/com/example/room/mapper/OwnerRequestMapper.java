package com.example.room.mapper;

import com.example.room.dto.response.OwnerRequestResponse;
import com.example.room.model.OwnerRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OwnerRequestMapper {

    @Mapping(source = "user.id",       target = "userId")
    @Mapping(source = "user.fullName", target = "userName")
    @Mapping(source = "user.phone", target = "phoneNumber")
    @Mapping(source = "user.email", target = "email")
    @Mapping(source = "user.citizenId", target = "citizenId")
    OwnerRequestResponse toResponse(OwnerRequest request);
}