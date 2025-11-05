package com.example.room.service.Impl;

import com.example.room.dto.PageResponse;
import com.example.room.dto.request.PaymentCreateRequest;
import com.example.room.dto.request.PaymentUpdateRequest;
import com.example.room.dto.response.PaymentResponse;
import com.example.room.exception.InvalidDataException;
import com.example.room.exception.ResourceNotFoundException;
import com.example.room.mapper.PaymentMapper;
import com.example.room.model.Booking;
import com.example.room.model.Contract;
import com.example.room.model.Payment;
import com.example.room.repository.BookingRepository;
import com.example.room.repository.ContractRepository;
import com.example.room.repository.PaymentRepository;
import com.example.room.service.ContractService;
import com.example.room.service.InvoiceService;
import com.example.room.service.PaymentService;
import com.example.room.specification.PaymentSpecification;
import com.example.room.utils.Enums.BookingStatus;
import com.example.room.utils.Enums.ContractStatus;
import com.example.room.utils.Enums.PaymentMethod;
import com.example.room.utils.Enums.PaymentStatus;
import com.example.room.utils.Enums.PaymentType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.room.model.User;
import com.example.room.utils.Enums.RoleEnum;
import org.springframework.security.access.AccessDeniedException;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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

    @Override
    @Transactional
    public PaymentResponse createPayment(PaymentCreateRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy booking với ID: " + request.getBookingId()));
        if (booking.getStatus() != BookingStatus.CONFIRMED && request.getPaymentType() == PaymentType.DEPOSIT) {
        throw new InvalidDataException("Booking phải được xác nhận trước khi tạo thanh toán cọc.");
        }
        Payment payment = paymentMapper.toEntity(request);
        payment.setBooking(booking);
        payment.setPaymentStatus(PaymentStatus.PENDING);
        switch (request.getPaymentType()) {
            case DEPOSIT:
                payment.setAmount(booking.getRoom().getDeposit());
                break;
            case OTHER:
                payment.setAmount(request.getAmount());
                break;
        }

        Payment savedPayment = paymentRepository.save(payment);

        return paymentMapper.toResponse(savedPayment);
    }


   @Override
    @Transactional
    public PaymentResponse updatePaymentStatus(Long id, PaymentUpdateRequest request) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy payment với ID: " + id));
        
        if (request.getPaymentStatus() == PaymentStatus.PAID && payment.getPaymentStatus() != PaymentStatus.PAID) {
        payment.setPaymentDate(LocalDateTime.now());
        }

        payment.setPaymentStatus(request.getPaymentStatus());
        Payment updatedPayment = paymentRepository.save(payment);

        if (updatedPayment.getPaymentStatus() == PaymentStatus.PAID) {
            handlePaidPayment(updatedPayment);
        }

        return paymentMapper.toResponse(updatedPayment);
    }

    private void handlePaidPayment(Payment payment) {
        Booking booking = payment.getBooking();

        boolean contractExists = booking.getContracts() != null && !booking.getContracts().isEmpty();
        if (!contractExists && payment.getPaymentType() == PaymentType.DEPOSIT) {
            contractService.createContractFromBooking(booking.getId());
        }

        try {
            invoiceService.createInvoiceRecord(payment.getId());
        } catch (Exception e) {
            log.error(" Lỗi khi tạo hóa đơn cho payment ID {}: {}", payment.getId(), e.getMessage());
        }

    }

    @Override
    public PageResponse<PaymentResponse> getAllPayments(int page, int size, Long bookingId, PaymentType paymentType, PaymentMethod paymentMethod, PaymentStatus paymentStatus, LocalDateTime paymentDate, LocalDateTime createdAt) {
        Pageable pageable = PageRequest.of(page, size);

        Specification<Payment> spec = PaymentSpecification.filter(bookingId, paymentType, paymentMethod, paymentStatus, paymentDate, createdAt);

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
    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy payment với ID: " + id));
        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional
    public void generateMonthlyPayments() {
        LocalDate today = LocalDate.now();
        List<Contract> activeContracts = contractRepository.findByStatus(ContractStatus.ACTIVE);

        for (Contract contract : activeContracts) {
            LocalDate start = contract.getStartDate().toLocalDate();
            LocalDate end = (contract.getEndDate() != null) ? contract.getEndDate().toLocalDate() : today.plusYears(1);

            if (today.isBefore(start) || today.isAfter(end)) {
                continue;
            }

            Booking booking = contract.getBooking();

            boolean exists = paymentRepository.existsByBooking_IdAndPaymentTypeAndDescription(
                booking.getId(),
                PaymentType.MONTHLY,
                "Thanh toán tiền phòng tháng " + today.getMonthValue() + "/" + today.getYear()
            );

            if (!exists) {

                Payment monthlyPayment = Payment.builder()
                        .booking(booking)
                        .paymentType(PaymentType.MONTHLY)
                        .paymentStatus(PaymentStatus.PENDING)
                        .amount(booking.getRoom().getPrice())
                        .description("Thanh toán tiền phòng tháng " + today.getMonthValue() + "/" + today.getYear())
                        .build();

                Payment savedPayment = paymentRepository.save(monthlyPayment);

                try {
                    invoiceService.createInvoiceRecord(savedPayment.getId());
                } catch (Exception e) {
                    log.error("Lỗi khi tự động tạo hóa đơn cho payment ID {}: {}", savedPayment.getId(), e.getMessage());
                }
            }
        }
    }
}
