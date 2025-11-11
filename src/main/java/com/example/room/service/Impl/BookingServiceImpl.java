package com.example.room.service.Impl;

import com.example.room.dto.BaseResponse;
import com.example.room.dto.PageResponse;
import com.example.room.dto.request.BookingCreateRequest;
import com.example.room.dto.request.BookingEmailRequest;
import com.example.room.dto.request.BookingUpdateRequest;
import com.example.room.dto.request.PaymentCreateRequest;
import com.example.room.dto.response.BookingResponse;
import com.example.room.dto.response.UserResponse;
import com.example.room.exception.ForBiddenException;
import com.example.room.exception.InvalidDataException;
import com.example.room.exception.ResourceNotFoundException;
import com.example.room.mapper.BookingMapper;
import com.example.room.model.*;
import com.example.room.repository.BankAccountRepository;
import com.example.room.repository.BookingRepository;
import com.example.room.repository.RoomRepository;
import com.example.room.repository.UserRepository;
import com.example.room.service.BookingService;
import com.example.room.service.EmailService;
import com.example.room.service.PaymentService;
import com.example.room.utils.BankAccountUtils;
import com.example.room.utils.Enums.*;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper;
    private final EmailService emailService;
    private final PaymentService paymentService;
    private final BankAccountUtils bankAccountUtils;
    private final BankAccountRepository bankAccountRepository;

    @Value("${app.booking.expiration-hours}")
    private long expirationHours;

    @Override
    @Transactional
    public BaseResponse<BookingResponse> createBooking(BookingCreateRequest request) throws MessagingException {
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (room.getStatus() != RoomStatus.AVAILABLE) {
            throw new InvalidDataException("Phòng này hiện không có sẵn hoặc đã được người khác đặt.");
        }
        Booking booking = bookingMapper.toBooking(request);
        booking.setStartDate(request.getStartDate().atStartOfDay());
        if (request.getEndDate() != null) {
            booking.setEndDate(request.getEndDate().atStartOfDay());
        }
        booking.setRoom(room);
        booking.setUser(user);
        booking.setStatus(BookingStatus.PENDING);
        booking.setTotalPrice(room.getDeposit());
        booking.setExpirationDate(LocalDateTime.now().plusHours(expirationHours));

        booking = bookingRepository.save(booking);

        return BaseResponse.<BookingResponse>builder()
                .code(200)
                .message("Tạo booking thành công")
                .data(bookingMapper.toBookingResponse(booking))
                .build();
    }

    @Override
    public PageResponse<BookingResponse> getAllBookings(Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        boolean isAdmin = currentUser.getRole().getName().name().equals(RoleEnum.ADMIN.name());
        boolean isOwner = currentUser.getRole().getName().name().equals(RoleEnum.OWNER.name());
        boolean isRenter = currentUser.getRole().getName().name().equals(RoleEnum.RENTER.name());

        Page<Booking> bookings;

        if (isAdmin) {
            bookings = bookingRepository.findAll(pageable);
        } else if (isOwner) {
            bookings = bookingRepository.findByRoom_Owner_Id(currentUser.getId(), pageable);
        } else if (isRenter) {
            bookings = bookingRepository.findByUser_Id(currentUser.getId(), pageable);
        } else {
            throw new ForBiddenException("Your role is not authorized to access this resource.");
        }
        List<BookingResponse>  list = bookings.stream().map(booking -> bookingMapper.toBookingResponse(booking)).toList();
        return PageResponse.<BookingResponse>builder()
                .code(200)
                .data(list)
                .message("Lấy danh sách phòng ")
                .pageNumber(bookings.getNumber())
                .totalElements(bookings.getTotalElements())
                .pageSize(bookings.getSize())
                .totalPages(bookings.getTotalPages())
                .build();
    }

    @Override
    public BaseResponse<BookingResponse> getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        return BaseResponse.<BookingResponse>builder()
                .code(200)
                .message("Lấy chi tiết Booking theo id thành công")
                .data(bookingMapper.toBookingResponse(booking))
                .build();
    }

    @Override
    @Transactional
    public BaseResponse<BookingResponse> updateBookingStatus(Long id, BookingUpdateRequest request) throws MessagingException {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        Room room1 = booking.getRoom();
        User user = booking.getUser();
        User owner = room1.getOwner();
        if(booking.getStatus() == BookingStatus.PENDING && request.getStatus() ==BookingStatus.CONFIRMED){
            room1.setStatus(RoomStatus.RESERVED);
            roomRepository.save(room1);
            booking.setStatus(request.getStatus());
            booking = bookingRepository.saveAndFlush(booking);
            PaymentCreateRequest paymentCreateRequest = PaymentCreateRequest.builder()
                    .amount(new BigDecimal(0))
                    .bookingId(booking.getId())
                    .paymentType(PaymentType.DEPOSIT)
                    .build();
            paymentService.createPayment(paymentCreateRequest);
            BankAccount bankAccount = bankAccountRepository.findByUser_Id(owner.getId()).orElseThrow(
                    ()-> new ResourceNotFoundException("Chủ phòng chưa có tài khoản ngân hàng")
            );
            BookingEmailRequest bookingEmailRequest = BookingEmailRequest.builder()
                    .bookingId(booking.getId())
                    .endDate(booking.getEndDate())
                    .startDate(booking.getStartDate())
                    .ownerEmail(owner.getEmail())
                    .status(booking.getStatus())
                    .roomAddress(room1.getAddress())
                    .roomName(room1.getName())
                    .ownerPhone(owner.getPhone())
                    .ownerName(owner.getFullName())
                    .totalPrice(booking.getTotalPrice())
                    .userName(user.getFullName())
                    .note("Thanh toán trong vòng 24h để xác nhận đặt phòng")
                    //.linkQR(bankAccountUtils.generateVietQR(bankAccount.getBankCode(), bankAccount.getAccountNumber(),
                            //bankAccount.getAccountName(),booking.getTotalPrice(), description))
                    .build();
            emailService.sendBooking(bookingEmailRequest,user.getEmail());
        }

        if(booking.getStatus() == BookingStatus.CONFIRMED && request.getStatus() == BookingStatus.COMPLETED){
            room1.setStatus(RoomStatus.RENTED);
            roomRepository.save(room1);
            booking.setStatus(request.getStatus());
            booking = bookingRepository.save(booking);
            BookingEmailRequest bookingEmailRequest = BookingEmailRequest.builder()
                    .bookingId(booking.getId())
                    .endDate(booking.getEndDate())
                    .startDate(booking.getStartDate())
                    .ownerEmail(owner.getEmail())
                    .status(booking.getStatus())
                    .roomAddress(room1.getAddress())
                    .roomName(room1.getName())
                    .ownerPhone(owner.getPhone())
                    .ownerName(owner.getFullName())
                    .totalPrice(booking.getTotalPrice())
                    .userName(user.getFullName())
                    .note("Thanh toán đặt phòng thành công")
                    .build();
            emailService.sendBooking(bookingEmailRequest,user.getEmail());
        }
        if (request.getStatus() == BookingStatus.CANCELLED) {
            Room room = booking.getRoom();
            if (room != null) {
                room.setStatus(RoomStatus.AVAILABLE);
                roomRepository.save(room);
            }
        }
        
        return BaseResponse.<BookingResponse>builder()
                .code(200)
                .message("Cập nhật booking thành công")
                .data(bookingMapper.toBookingResponse(booking))
                .build();
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
        List<Booking> expiredBookings = bookingRepository.findByStatusAndExpirationDateBefore(
                BookingStatus.PENDING, 
                LocalDateTime.now()
        );
        if (expiredBookings.isEmpty()) {
            return;
        }
        
        log.info("Tìm thấy {} booking quá hạn cần xử lý.", expiredBookings.size());

        for (Booking booking : expiredBookings) {
            booking.setStatus(BookingStatus.CANCELLED);
            Room room = booking.getRoom();
            if (room != null && room.getStatus() == RoomStatus.RESERVED) {
                room.setStatus(RoomStatus.AVAILABLE);
                roomRepository.save(room);
                log.info("Đã giải phóng phòng ID {} từ booking ID {}.", room.getId(), booking.getId());
            }
        }
        bookingRepository.saveAll(expiredBookings);
    }

}