package com.example.room.mapper;

import com.example.room.dto.request.BookingCreateRequest;
import com.example.room.dto.response.BookingResponse;
import com.example.room.model.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(target = "room", ignore = true)
    @Mapping(target = "user", ignore = true)
    Booking toBooking(BookingCreateRequest request);


    @Mapping(source = "room.name", target = "roomName")
    @Mapping(source = "user.fullName", target = "nameUser")
    BookingResponse toBookingResponse(Booking booking);
}