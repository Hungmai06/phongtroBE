package com.example.room.service;

import com.example.room.dto.request.BookingCreateRequest;
import com.example.room.dto.request.BookingUpdateRequest;
import com.example.room.dto.response.BookingResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BookingService {
    BookingResponse createBooking(BookingCreateRequest request);

    Page<BookingResponse> getAllBookings(Pageable pageable);

    BookingResponse getBookingById(Long id);

    BookingResponse updateBookingStatus(Long id, BookingUpdateRequest request);

    void deleteBooking(Long id);
    void cancelExpiredBookings();
}