package com.example.room.dto.request;

import com.example.room.utils.Enums.BookingStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BookingUpdateRequest {
    private BookingStatus status;
    private String hourDate;
    private LocalDate appointmentDate;
}