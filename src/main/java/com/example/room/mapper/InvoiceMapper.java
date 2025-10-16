package com.example.room.mapper;

import com.example.room.dto.response.InvoiceResponse;
import com.example.room.model.Invoice;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface InvoiceMapper {
    InvoiceResponse toResponse(Invoice invoice);
}