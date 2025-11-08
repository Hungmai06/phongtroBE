package com.example.room.mapper;

import com.example.room.dto.response.BankAccountResponse;
import com.example.room.model.BankAccount;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BankAccountMapper {

    @Mapping(target = "userId", source = "user.id")
    BankAccountResponse toResponse(BankAccount e);
}
