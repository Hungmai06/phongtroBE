package com.example.room.service.Impl;

import com.example.room.dto.BaseResponse;
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
import com.example.room.utils.Enums.BookingStatus;
import com.example.room.utils.Enums.ContractStatus;
import com.example.room.utils.Enums.PaymentStatus;
import com.example.room.utils.Enums.PaymentType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.room.model.User;
import com.example.room.utils.Enums.RoleEnum;
import org.springframework.security.access.AccessDeniedException;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
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

        switch (request.getPaymentType()) {
            case DEPOSIT:
                payment.setAmount(booking.getRoom().getDeposit());
                break;
            case OTHER:
        
                break;
        }

        // Automatically set the payment date if the status is PAID upon creation
        if (payment.getPaymentStatus() == PaymentStatus.PAID) {
        payment.setPaymentDate(LocalDateTime.now());
        }
        Payment savedPayment = paymentRepository.save(payment);

        if (savedPayment.getPaymentStatus() == PaymentStatus.PAID) {
            handlePaidPayment(savedPayment);
        }

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
            contractService.createContractFromBooking(booking);
            log.info("✅ Đã tạo hợp đồng cho booking ID {}", booking.getId());
        }

        try {
            invoiceService.createInvoiceRecord(payment);
            log.info("🧾 Đã tạo hóa đơn cho payment ID {}", payment.getId());
        } catch (Exception e) {
            log.error("❌ Lỗi khi tạo hóa đơn cho payment ID {}: {}", payment.getId(), e.getMessage());
        }

        log.info("📧 Đã gửi email xác nhận thanh toán cho renter.");
    }

     @Override
    public PageResponse<PaymentResponse> getAllPayments(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Kiểm tra vai trò của người dùng hiện tại
        boolean isAdmin = currentUser.getRole().getName().name().equals(RoleEnum.ADMIN.name());
        boolean isOwner = currentUser.getRole().getName().name().equals(RoleEnum.OWNER.name());
        boolean isRenter = currentUser.getRole().getName().name().equals(RoleEnum.RENTER.name());

        Page<Payment> paymentPage;

        if (isAdmin) {
            // ADMIN: Lấy tất cả các thanh toán
            paymentPage = paymentRepository.findAll(pageable);
        } else if (isOwner) {
            // OWNER: Lấy các thanh toán liên quan đến phòng của họ
            paymentPage = paymentRepository.findByBooking_Room_Owner_Id(currentUser.getId(), pageable);
        } else if (isRenter) {
            // RENTER: Lấy các thanh toán của chính họ
            paymentPage = paymentRepository.findByBooking_User_Id(currentUser.getId(), pageable);
        } else {
            // Vai trò không được phép
            throw new AccessDeniedException("Your role is not authorized to access payment records.");
        }

        List<PaymentResponse> responses = paymentPage.getContent()
                .stream()
                .map(paymentMapper::toResponse)
                .collect(Collectors.toList());

        return PageResponse.<PaymentResponse>builder()
                .code(200)
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
        log.info("Bắt đầu quét và tạo thanh toán hàng tháng cho {} hợp đồng đang hoạt động...", activeContracts.size());

        for (Contract contract : activeContracts) {
            LocalDate start = contract.getStartDate().toLocalDate();
            // Xử lý trường hợp không có ngày kết thúc
            LocalDate end = (contract.getEndDate() != null) ? contract.getEndDate().toLocalDate() : today.plusYears(1);

            if (today.isBefore(start) || today.isAfter(end)) {
                continue; // Bỏ qua hợp đồng chưa bắt đầu hoặc đã kết thúc
            }

            LocalDateTime monthStart = today.withDayOfMonth(1).atStartOfDay();
            LocalDateTime monthEnd = today.with(TemporalAdjusters.lastDayOfMonth()).atTime(23, 59, 59);
            Booking booking = contract.getBooking();

            // Sửa logic kiểm tra: Dựa trên mô tả để tránh lỗi paymentDate
            boolean exists = paymentRepository.existsByBooking_IdAndPaymentTypeAndDescription(
                booking.getId(),
                PaymentType.MONTHLY,
                "Thanh toán tiền phòng tháng " + today.getMonthValue() + "/" + today.getYear()
            );

            if (!exists) {

                // 1. Tạo thanh toán PENDING (KHÔNG có paymentDate)
                Payment monthlyPayment = Payment.builder()
                        .booking(booking)
                        .paymentType(PaymentType.MONTHLY)
                        .paymentStatus(PaymentStatus.PENDING)
                        .amount(booking.getRoom().getPrice())
                        .description("Thanh toán tiền phòng tháng " + today.getMonthValue() + "/" + today.getYear())
                        .build();

                Payment savedPayment = paymentRepository.save(monthlyPayment);
                log.info("✅ Đã tạo payment MONTHLY cho hợp đồng ID {}", contract.getId());

                // 2. TỰ ĐỘNG TẠO HÓA ĐƠN NGAY LẬP TỨC
                try {
                    invoiceService.createInvoiceRecord(savedPayment);
                    log.info("🧾 Đã tự động tạo hóa đơn cho payment ID {}", savedPayment.getId());
                } catch (Exception e) {
                    log.error("❌ Lỗi khi tự động tạo hóa đơn cho payment ID {}: {}", savedPayment.getId(), e.getMessage());
                }
            }
        }
    }
}
