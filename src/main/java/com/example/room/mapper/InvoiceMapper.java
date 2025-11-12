package com.example.room.mapper;

import com.example.room.dto.response.InvoiceResponse;
import com.example.room.model.Invoice;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", 
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {PaymentMapper.class, ContractMapper.class})
public interface InvoiceMapper {
    InvoiceResponse toResponse(Invoice invoice);
}