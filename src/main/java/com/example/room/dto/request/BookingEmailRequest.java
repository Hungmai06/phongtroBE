package com.example.room.dto.request;

import com.example.room.utils.Enums.BookingStatus;
import lombok.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.BitSet;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingEmailRequest {
    private String userName;
    private Long bookingId;
    private String roomName;
    private BigDecimal totalPrice;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String roomAddress;
    private BookingStatus status;
    private String ownerName;
    private String ownerEmail;
    private String ownerPhone;
    private String note;
    private String linkQR;
}
