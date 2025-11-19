package com.example.room.service.Impl;

import com.example.room.dto.BaseResponse;
import com.example.room.dto.PageResponse;
import com.example.room.dto.response.InvoiceResponse;
import com.example.room.dto.response.PaymentResponse;
import com.example.room.dto.response.UserResponse;
import com.example.room.exception.ForBiddenException;
import com.example.room.exception.ResourceNotFoundException;
import com.example.room.mapper.ContractMapper;
import com.example.room.mapper.InvoiceMapper;
import com.example.room.mapper.PaymentMapper;
import com.example.room.mapper.UserMapper;
import com.example.room.model.Contract;
import com.example.room.model.Invoice;
import com.example.room.model.Payment;
import com.example.room.model.User;
import com.example.room.repository.ContractRepository;
import com.example.room.repository.InvoiceRepository;
import com.example.room.repository.PaymentRepository;
import com.example.room.service.EmailService;
import com.example.room.service.InvoiceService;
import com.example.room.service.PdfGeneratorService;
import com.example.room.utils.Enums.InvoiceStatus;
import com.example.room.utils.Enums.RoleEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final ContractRepository contractRepository;
    private final PaymentRepository paymentRepository;
    private final InvoiceMapper invoiceMapper;
    private final PdfGeneratorService pdfGeneratorService;
    private final EmailService emailService;
    private final PaymentMapper paymentMapper;
    private final UserMapper userMapper;
    private final ContractMapper contractMapper;

    @Override
    @Transactional
    public BaseResponse<InvoiceResponse> createInvoiceRecord(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));

        Optional<Invoice> existingInvoice = invoiceRepository.findByPaymentId(payment.getId());
        if (existingInvoice.isPresent()) {
            InvoiceResponse existingResponse = invoiceMapper.toResponse(existingInvoice.get());
            return BaseResponse.<InvoiceResponse>builder()
                    .code(201)
                    .message("Invoice already exists for this payment.")
                    .data(existingResponse)
                    .build();
        }

        Contract contract = contractRepository.findByBookingId(payment.getBooking().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found for booking ID: " + payment.getBooking().getId()));

        Invoice newInvoice = Invoice.builder()
                .invoiceNumber(generateInvoiceNumber())
                .issueDate(LocalDateTime.now())
                .totalAmount(payment.getAmount())
                .status(InvoiceStatus.CREATED)
                .payment(payment)
                .contract(contract)
                .user(payment.getBooking().getUser())
                .build();

        Invoice savedInvoice = invoiceRepository.save(newInvoice);

        processAndSendInvoice(savedInvoice.getId());

        InvoiceResponse response = invoiceMapper.toResponse(savedInvoice);
        return BaseResponse.<InvoiceResponse>builder()
                .code(201)
                .message("Invoice created successfully.")
                .data(response)
                .build();
    }


    @Async
    @Override
    public void processAndSendInvoice(Long invoiceId) {
        try {
            Invoice invoice = invoiceRepository.findById(invoiceId)
                    .orElseThrow(() -> new ResourceNotFoundException("Invoice not found for async processing: " + invoiceId));

            String outputPath = System.getProperty("user.dir") + "/uploads/invoices/invoice-" + invoice.getInvoiceNumber() + ".pdf";
            Context context = new Context();
            context.setVariable("invoiceNumber", invoice.getInvoiceNumber());
            context.setVariable("issueDate", invoice.getIssueDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            context.setVariable("customerName", invoice.getUser().getFullName());
            context.setVariable("customerEmail", invoice.getUser().getEmail());
            context.setVariable("customerPhone", invoice.getUser().getPhone());
            context.setVariable("roomName", invoice.getContract().getBooking().getRoom().getName());
            context.setVariable("roomAddress", invoice.getContract().getBooking().getRoom().getAddress());
            context.setVariable("paymentType", invoice.getPayment().getPaymentType().name());
            context.setVariable("description", invoice.getPayment().getDescription());
            context.setVariable("amount", invoice.getPayment().getAmount());
            context.setVariable("paymentDate", invoice.getPayment().getPaymentDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            context.setVariable("totalAmount", invoice.getTotalAmount());

            pdfGeneratorService.generatePdf("invoice-template", context, outputPath);

            String subject = "Hóa đơn thanh toán: " + invoice.getInvoiceNumber();
            String body = "Kính gửi " + invoice.getUser().getFullName() + ",\n\n" +
                    "Vui lòng tìm hóa đơn thanh toán đính kèm.";
            emailService.sendEmailWithAttachment(invoice.getUser().getEmail(), subject, body, outputPath);

            invoice.setStatus(InvoiceStatus.SENT);
            invoiceRepository.save(invoice);


        } catch (ResourceNotFoundException e) {
            log.error("Không thể xử lý hóa đơn không đồng bộ: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Lỗi khi xử lý hóa đơn: {}", e.getMessage(), e);
        }
    }

    private String generateInvoiceNumber() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int randomNum = ThreadLocalRandom.current().nextInt(1000, 10000);
        return "INV-" + datePart + "-" + randomNum;
    }

    @Override
    public PageResponse<InvoiceResponse> getInvoices(Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        boolean isAdmin = currentUser.getRole().getName().name().equals(RoleEnum.ADMIN.name());
        boolean isOwner = currentUser.getRole().getName().name().equals(RoleEnum.OWNER.name());
        boolean isRenter = currentUser.getRole().getName().name().equals(RoleEnum.RENTER.name());

        Page<Invoice> invoices;

        if (isAdmin) {
            invoices = invoiceRepository.findAll(pageable);
        } else if (isOwner) {
            invoices = invoiceRepository.findByContract_Booking_Room_Owner_Id(currentUser.getId(), pageable);
        } else if (isRenter) {
            invoices = invoiceRepository.findByUser_Id(currentUser.getId(), pageable);
        } else {
            throw new ForBiddenException("Unauthorized role");
        }
        List<Payment> payment = invoices.stream().map(Invoice::getPayment).toList();
        List<InvoiceResponse> invoiceResponses = new ArrayList<>();
        for(Payment x:payment){
            InvoiceResponse invoiceResponse = InvoiceResponse.builder()
                    .contract(contractMapper.toResponse(x.getContract()))
                    .payment(paymentMapper.toResponse(x))
                    .user(userMapper.toResponse(currentUser))
                    .invoiceNumber(x.getInvoice().getInvoiceNumber())
                    .id(x.getInvoice().getId())
                    .issueDate(x.getInvoice().getIssueDate())
                    .totalAmount(x.getInvoice().getTotalAmount())
                    .status(x.getInvoice().getStatus())
                    .build();
            invoiceResponses.add(invoiceResponse);
        }

        return PageResponse.<InvoiceResponse>builder()
                .pageNumber(invoices.getNumber())
                .pageSize(invoices.getSize())
                .totalElements(invoices.getTotalElements())
                .totalPages(invoices.getTotalPages())
                .code(200)
                .message("Lấy danh sách hóa đơn thành công")
                .data(invoiceResponses)
                .build();
    }

    @Override
    public BaseResponse<InvoiceResponse> getInvoiceById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn ID: " + id));

        return BaseResponse.<InvoiceResponse>builder()
                .code(200)
                .data(invoiceMapper.toResponse(invoice))
                .message("Lấy thông tin hóa đơn thành công")
                .build();
    }

    @Override
    public byte[] downloadInvoice(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn ID: " + id));

        invoice.setStatus(InvoiceStatus.DOWNLOADED);
        invoiceRepository.save(invoice);

        log.info("📄 Hóa đơn #{} đã được tải xuống.", invoice.getInvoiceNumber());
        return new byte[0];
    }

    @Override
    public void sendInvoiceByEmail(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn ID: " + id));

        invoice.setStatus(InvoiceStatus.SENT);
        invoiceRepository.save(invoice);

        log.info("📧 Hóa đơn #{} đã được gửi lại qua email.", invoice.getInvoiceNumber());
    }

    @Override
    public void cancelInvoice(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn ID: " + id));

        invoice.setStatus(InvoiceStatus.CANCELLED);
        invoiceRepository.save(invoice);

        log.warn("⚠️ Hóa đơn #{} đã bị hủy bởi ADMIN.", invoice.getInvoiceNumber());
    }
}
