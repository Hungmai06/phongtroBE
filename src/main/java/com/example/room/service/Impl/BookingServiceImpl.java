package com.example.room.service.Impl;

import com.example.room.dto.request.BookingCreateRequest;
import com.example.room.dto.request.BookingUpdateRequest;
import com.example.room.dto.response.BookingResponse;
import com.example.room.exception.ForBiddenException;
import com.example.room.exception.InvalidDataException;
import com.example.room.exception.ResourceNotFoundException;
import com.example.room.mapper.BookingMapper;
import com.example.room.model.Booking;
import com.example.room.model.Invoice;
import com.example.room.model.Room;
import com.example.room.model.User;
import com.example.room.repository.BookingRepository;
import com.example.room.repository.RoomRepository;
import com.example.room.repository.UserRepository;
import com.example.room.service.BookingService;
import com.example.room.utils.Enums.BookingStatus;
import com.example.room.utils.Enums.RoleEnum;
import com.example.room.utils.Enums.RoomStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;
import lombok.extern.slf4j.Slf4j;
import com.example.room.utils.Enums.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
public class BookingServiceImpl implements BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingMapper bookingMapper;

    @Value("${app.booking.expiration-hours}")
    private long expirationHours;

    @Override
    @Transactional
    public BookingResponse createBooking(BookingCreateRequest request) {
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (room.getStatus() != RoomStatus.AVAILABLE) {
            throw new InvalidDataException("Phòng này hiện không có sẵn hoặc đã được người khác đặt.");
        }
        Booking booking = bookingMapper.toBooking(request);
        booking.setRoom(room);
        booking.setUser(user);
        booking.setStatus(BookingStatus.PENDING);
        booking.setExpirationDate(LocalDateTime.now().plusHours(expirationHours));

        room.setStatus(RoomStatus.RESERVED);
        booking = bookingRepository.save(booking);
        return bookingMapper.toBookingResponse(booking);
    }

    @Override
    public Page<BookingResponse> getAllBookings(Pageable pageable) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        boolean isAdmin = currentUser.getRole().getName().name().equals(RoleEnum.ADMIN.name());
        boolean isOwner = currentUser.getRole().getName().name().equals(RoleEnum.OWNER.name());
        boolean isRenter = currentUser.getRole().getName().name().equals(RoleEnum.RENTER.name());

        Page<Booking> bookings;

        if (isAdmin) {
            // ADMIN: Lấy tất cả các booking
            bookings = bookingRepository.findAll(pageable);
        } else if (isOwner) {
            // OWNER: Lấy các booking liên quan đến phòng của họ
            bookings = bookingRepository.findByRoom_Owner_Id(currentUser.getId(), pageable);
        } else if (isRenter) {
            // RENTER: Lấy các booking của chính họ
            bookings = bookingRepository.findByUser_Id(currentUser.getId(), pageable);
        } else {
            throw new ForBiddenException("Your role is not authorized to access this resource.");
        }

        return bookings.map(bookingMapper::toBookingResponse);
    }

    @Override
    public BookingResponse getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        return bookingMapper.toBookingResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse updateBookingStatus(Long id, BookingUpdateRequest request) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        booking.setStatus(request.getStatus());
        booking = bookingRepository.save(booking);
        if (request.getStatus() == BookingStatus.CANCELLED) {
            Room room = booking.getRoom();
            if (room != null) {
                room.setStatus(RoomStatus.AVAILABLE);
                roomRepository.save(room);
            }
        }
        
        return bookingMapper.toBookingResponse(booking);
    }

    @Override
    @Transactional
    public void deleteBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        
        booking.setDeleted(true);
        bookingRepository.save(booking);
    }
    @Override
    @Transactional
    public void cancelExpiredBookings() {
        // Tìm tất cả booking đang PENDING và đã qua ngày hết hạn
        List<Booking> expiredBookings = bookingRepository.findByStatusAndExpirationDateBefore(
                BookingStatus.PENDING, 
                LocalDateTime.now()
        );

        if (expiredBookings.isEmpty()) {
            return; // Không có gì để xử lý
        }
        
        log.info("Tìm thấy {} booking quá hạn cần xử lý.", expiredBookings.size());

        for (Booking booking : expiredBookings) {
            // Chuyển trạng thái booking thành CANCELLED
            booking.setStatus(BookingStatus.CANCELLED);
            
            // Lấy phòng liên quan và chuyển trạng thái về AVAILABLE
            Room room = booking.getRoom();
            if (room != null && room.getStatus() == RoomStatus.RESERVED) {
                room.setStatus(RoomStatus.AVAILABLE);
                roomRepository.save(room);
                log.info("Đã giải phóng phòng ID {} từ booking ID {}.", room.getId(), booking.getId());
            }
        }
        
        // Lưu lại thay đổi trạng thái của các booking
        bookingRepository.saveAll(expiredBookings);
    }

}