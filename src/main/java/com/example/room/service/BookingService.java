package com.example.room.service;

import com.example.room.dto.BaseResponse;
import com.example.room.dto.PageResponse;
import com.example.room.dto.request.BookingCreateRequest;
import com.example.room.dto.request.BookingUpdateRequest;
import com.example.room.dto.response.BookingResponse;
import jakarta.mail.MessagingException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BookingService {
    BaseResponse<BookingResponse> createBooking(BookingCreateRequest request) throws MessagingException;

    PageResponse<BookingResponse> getAllBookings(Integer page, Integer size);

    BaseResponse<BookingResponse> getBookingById(Long id);

    BaseResponse<BookingResponse> updateBookingStatus(Long id, BookingUpdateRequest request) throws MessagingException;

    void deleteBooking(Long id);
    void cancelExpiredBookings();
}