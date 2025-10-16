package com.example.room.dto.request;

import com.example.room.utils.Enums.BookingStatus;
import lombok.Data;

@Data
public class BookingUpdateRequest {
    private BookingStatus status;
}