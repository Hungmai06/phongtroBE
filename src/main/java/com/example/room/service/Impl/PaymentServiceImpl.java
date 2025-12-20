package com.example.room.service.Impl;

import com.example.room.dto.BaseResponse;
import com.example.room.dto.PageResponse;
import com.example.room.dto.request.PaymentCreateRequest;
import com.example.room.dto.request.PaymentMonthlyRequest;
import com.example.room.dto.request.PaymentUpdateRequest;
import com.example.room.dto.response.OwnerRevenuePeriodDto;
import com.example.room.dto.response.PaymentResponse;
import com.example.room.dto.response.RevenueByOwnerResponse;
import com.example.room.exception.InvalidDataException;
import com.example.room.exception.ResourceNotFoundException;
import com.example.room.mapper.PaymentMapper;
import com.example.room.model.*;
import com.example.room.repository.*;
import com.example.room.service.*;
import com.example.room.specification.PaymentSpecification;
import com.example.room.utils.BankAccountUtils;
import com.example.room.utils.Enums.BookingStatus;
import com.example.room.utils.Enums.ContractStatus;
import com.example.room.utils.Enums.PaymentMethod;
import com.example.room.utils.Enums.PaymentStatus;
import com.example.room.utils.Enums.PaymentType;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.room.utils.Enums.RoleEnum;
import org.springframework.security.access.AccessDeniedException;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final ContractRepository contractRepository;
    private final PaymentMapper paymentMapper;
    private final ContractService contractService;
    private final InvoiceService invoiceService;
    private final RoomRepository roomRepository;
    private final RoomServiceUsageRepository roomServiceUsageRepository;
    private final EmailService emailService;
    private final BankAccountUtils bankAccountUtils;

    @Override
    @Transactional
    public BaseResponse<PaymentResponse> createPayment(PaymentCreateRequest request) throws MessagingException {
        Room room = null;

        if (request.getRoomId() != null) {
            room = roomRepository.findById(request.getRoomId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng với ID: " + request.getRoomId()));
            List<Payment> payments = paymentRepository.findAll();
            List<PaymentResponse> paymentResponses = payments.stream().map(paymentMapper::toResponse).collect(Collectors.toList());
            for (PaymentResponse paymentResponse : paymentResponses) {
                if (paymentResponse.getRoomId() == room.getId() && paymentResponse.getPaymentType() == PaymentType.MONTHLY
                        && paymentResponse.getPaymentStatus() == PaymentStatus.PENDING && paymentResponse.getPaymentPeriod().equals(request.getPaymentPeriod())) {
                    throw new InvalidDataException("Đã tồn tại một khoản thanh toán tháng chưa được xử lý cho phòng này.");
                }
            }
        }
        Payment payment = Payment.builder()
                .paymentMethod(request.getPaymentMethod())
                .paymentPeriod(request.getPaymentPeriod())
                .paymentType(request.getPaymentType())
                .paymentStatus(PaymentStatus.PENDING)
                .build();
        PaymentMonthlyRequest monthlyRequest = null;
        String recipientEmail = null;

        switch (request.getPaymentType()) {
            case DEPOSIT:
                if(request.getBookingId() != null){
                    Booking booking = bookingRepository.findById(request.getBookingId())
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Không tìm thấy booking với ID: " + request.getBookingId()));
                    if (booking.getStatus() != BookingStatus.CONFIRMED && request.getPaymentType() == PaymentType.DEPOSIT) {
                        throw new InvalidDataException("Booking phải được xác nhận trước khi tạo thanh toán cọc.");
                    }
                    String description = "Thanh toán cọc cho đặt phòng " + booking.getRoom().getName() + " " + UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
                    payment.setDescription(description);
                    payment.setBooking(booking);
                    payment.setAmount(booking.getRoom().getDeposit());
                }
                break;
            case MONTHLY: {
                Contract contract;
                Booking booking = null;

                    contract = contractRepository
                            .findActiveContractByRoomId(room.getId(), ContractStatus.ACTIVE)
                            .orElse(null);

                    if (contract != null) {
                        booking = contract.getBooking();
                        if (booking != null) {
                            payment.setBooking(booking);
                        }
                        payment.setContract(contract);
                    }

                List<RoomServiceUsage> serviceUsages =
                        roomServiceUsageRepository.findByRoomIdAndMonth(room.getId(), request.getPaymentPeriod());

                BigDecimal servicesTotal = BigDecimal.ZERO;
                for (RoomServiceUsage usage : serviceUsages) {
                    servicesTotal = servicesTotal.add(usage.getTotalPrice());
                }

                BigDecimal baseRent = room.getPrice();
                BigDecimal grandTotal = baseRent.add(servicesTotal);
                payment.setAmount(grandTotal);

                String description = "Thanh toán tiền phòng " + room.getName()
                        + " tháng " + request.getPaymentPeriod() + " "
                        + UUID.randomUUID().toString().replace("-", "").substring(0, 6);
                payment.setDescription(description);

                // Build service items
                List<PaymentMonthlyRequest.ServiceItem> serviceItems = new ArrayList<>();
                for (RoomServiceUsage usage : serviceUsages) {
                    PaymentMonthlyRequest.ServiceItem item = PaymentMonthlyRequest.ServiceItem.builder()
                            .quantityUsed(usage.getQuantityUsed())
                            .quantityOld(usage.getQuantityOld())
                            .quantityNew(usage.getQuantityNew())
                            .name(usage.getName())
                            .totalPrice(usage.getTotalPrice())
                            .build();
                    serviceItems.add(item);
                }

                if (room.getOwner() == null) {
                    throw new InvalidDataException("Phòng không có chủ sở hữu hợp lệ.");
                }

                BankAccount bankAccount = room.getOwner().getBankAccount();
                if (bankAccount == null) {
                    throw new InvalidDataException("Chủ phòng chưa cấu hình tài khoản ngân hàng.");
                }

                if (booking != null && booking.getUser() != null && booking.getUser().getEmail() != null) {
                    recipientEmail = booking.getUser().getEmail();
                } else if (room.getOwner().getEmail() != null) {
                    recipientEmail = room.getOwner().getEmail();
                } else {
                    throw new InvalidDataException("Không tìm thấy email người nhận để gửi hóa đơn.");
                }

                // TẠM thời lưu lại data cần cho email, chưa set paymentId ở đây
                monthlyRequest = PaymentMonthlyRequest.builder()
                        .paymentPeriod(request.getPaymentPeriod())
                        .baseRent(room.getPrice())
                        .services(serviceItems)
                        .roomAddress(room.getAddress())
                        .roomName(room.getName())
                        .ownerEmail(room.getOwner().getEmail())
                        .ownerName(room.getOwner().getFullName())
                        .ownerPhone(room.getOwner().getPhone())
                        .servicesTotal(servicesTotal)
                        .grandTotal(grandTotal)
                        .vietQR(bankAccountUtils.generateVietQR(
                                bankAccount.getBankCode(),
                                bankAccount.getAccountNumber(),
                                bankAccount.getAccountName(),
                                grandTotal,
                                description))
                        .build();

                break;
            }
        }

        // Save duy nhất 1 lần
        Payment savedPayment = paymentRepository.save(payment);

        // Sau khi có id rồi thì update cho MONTHLY request & gửi mail
        if (request.getPaymentType() == PaymentType.MONTHLY && monthlyRequest != null) {
            monthlyRequest.setPaymentId(savedPayment.getId());
            monthlyRequest.setPaymentPeriod(savedPayment.getPaymentPeriod());
            emailService.sendPaymentMonthly(monthlyRequest, recipientEmail);
        }

        return BaseResponse.<PaymentResponse>builder()
                .data(paymentMapper.toResponse(savedPayment))
                .code(201)
                .message("Payment created")
                .build();
    }


    @Override
    @Transactional
    public BaseResponse<PaymentResponse> updatePaymentStatus(Long id, PaymentUpdateRequest request) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy payment với ID: " + id));
        
        if (request.getPaymentStatus() == PaymentStatus.PAID && payment.getPaymentStatus() != PaymentStatus.PAID) {
            payment.setPaymentDate(LocalDateTime.now());
        }

        payment.setPaymentStatus(request.getPaymentStatus());
        Payment updatedPayment = paymentRepository.save(payment);

        if (updatedPayment.getPaymentStatus() == PaymentStatus.PAID) {
            handlePaidPayment(updatedPayment,request.getStartDate(), request.getEndDate());
        }

        return BaseResponse.<PaymentResponse>builder()
                .data(paymentMapper.toResponse(updatedPayment))
                .code(201)
                .message("Payment updated")
                .build();
   }

    private void handlePaidPayment(Payment payment,LocalDate startDate, LocalDate endDate) {
        Booking booking = payment.getBooking();

        if (booking != null) {
            boolean contractExists = booking.getContracts() != null && !booking.getContracts().isEmpty();
            if (!contractExists && payment.getPaymentType() == PaymentType.DEPOSIT) {
                contractService.createContractPayment(payment.getId(),startDate,endDate);
            }
        } else {
            // No booking associated (e.g. monthly payment created from roomId) — skip contract creation.
            log.debug("Paid payment id {} has no booking associated; skipping contract creation.", payment.getId());
        }

         try {
             invoiceService.createInvoiceRecord(payment.getId());
         } catch (Exception e) {
             log.error(" Lỗi khi tạo hóa đơn cho payment ID {}: {}", payment.getId(), e.getMessage());
         }

     }

    @Override
    public PageResponse<PaymentResponse> getAllPayments(int page, int size, Long bookingId, PaymentType paymentType, PaymentMethod paymentMethod, PaymentStatus paymentStatus, LocalDateTime paymentDate, LocalDate paymentPeriod) {
        Pageable pageable = PageRequest.of(page, size);

        Specification<Payment> spec = PaymentSpecification.filter(bookingId, paymentType, paymentMethod, paymentStatus, paymentDate, paymentPeriod);

        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        boolean isAdmin = currentUser.getRole().getName().name().equals(RoleEnum.ADMIN.name());
        boolean isOwner = currentUser.getRole().getName().name().equals(RoleEnum.OWNER.name());
        boolean isRenter = currentUser.getRole().getName().name().equals(RoleEnum.RENTER.name());

        Page<Payment> paymentPage;

        if (isAdmin) {
            if (spec == null) {
                paymentPage = paymentRepository.findAll(pageable);
            } else {
                paymentPage = paymentRepository.findAll(spec, pageable);
            }
        } else if (isOwner) {
            Specification<Payment> ownerSpec = (root, query, cb) -> cb.equal(root.get("booking").get("room").get("owner").get("id"), currentUser.getId());
            Specification<Payment> combined = (spec == null) ? ownerSpec : spec.and(ownerSpec);
            paymentPage = paymentRepository.findAll(combined, pageable);
        } else if (isRenter) {
            Specification<Payment> renterSpec = (root, query, cb) -> cb.equal(root.get("booking").get("user").get("id"), currentUser.getId());
            Specification<Payment> combined = (spec == null) ? renterSpec : spec.and(renterSpec);
            paymentPage = paymentRepository.findAll(combined, pageable);
        } else {
            throw new AccessDeniedException("Your role is not authorized to access payment records.");
        }

        List<PaymentResponse> responses = paymentPage.getContent().stream().map(paymentMapper::toResponse).collect(Collectors.toList());

        return PageResponse.<PaymentResponse>builder()
                .code(200)
                .pageSize(paymentPage.getSize())
                .pageNumber(paymentPage.getNumber())
                .totalElements(paymentPage.getTotalElements())
                .totalPages(paymentPage.getTotalPages())
                .message("Lấy danh sách thanh toán thành công")
                .data(responses)
                .build();
    }


    @Override
    public BaseResponse<PaymentResponse> getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy payment với ID: " + id));
        return BaseResponse.<PaymentResponse>builder()
                .message("Lấy thông tin thanh toán thành công")
                .code(200)
                .data(paymentMapper.toResponse(payment))
                .build();
    }

    @Override
    public BaseResponse<PaymentResponse> getPaymentByRoomId(Long roomId) {
        List<Payment> payments = paymentRepository.findAll();
        List<PaymentResponse> paymentResponses = payments.stream().map(paymentMapper::toResponse).collect(Collectors.toList());
        for(PaymentResponse paymentResponse : paymentResponses) {
            if(paymentResponse.getRoomId()==roomId && paymentResponse.getPaymentType()==PaymentType.MONTHLY){
                return BaseResponse.<PaymentResponse>builder()
                        .message("Lấy thông tin thanh toán thành công")
                        .code(200)
                        .data(paymentResponse)
                        .build();
            }
        }
        return null;
    }

    @Override
    public BaseResponse<BigDecimal> ownerRevenueInMonth(Long ownerId, String period) {

        if (ownerId == null || period == null) {
            throw new InvalidDataException("ownerId và period (YYYY-MM) không được null.");
        }

        BigDecimal revenue = paymentRepository.getOwnerRevenueInMonth(PaymentStatus.PAID,ownerId, period);

        return BaseResponse.<BigDecimal>builder()
                .code(200)
                .message("OK")
                .data(revenue)
                .build();
    }

    @Override
    public void deletePayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy payment với ID: " + id));
        payment.setDeleted(true);
        paymentRepository.save(payment);
    }

}
