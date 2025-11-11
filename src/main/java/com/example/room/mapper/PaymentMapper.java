package com.example.room.mapper;

import com.example.room.dto.request.PaymentCreateRequest;
import com.example.room.dto.response.PaymentResponse;
import com.example.room.model.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMapper {

    @Mapping(source = "booking.id", target = "bookingId")
    PaymentResponse toResponse(Payment payment);
}