package com.example.room.mapper;

import com.example.room.dto.response.ContractResponse;
import com.example.room.model.Contract;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ContractMapper {
    ContractMapper INSTANCE = Mappers.getMapper(ContractMapper.class);

    @Mapping(source = "booking.id", target = "bookingId")
    ContractResponse toResponse(Contract contract);
}