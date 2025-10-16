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


    @Mapping(source = "room.id", target = "roomId")
    @Mapping(source = "user.id", target = "userId")
    BookingResponse toBookingResponse(Booking booking);
}