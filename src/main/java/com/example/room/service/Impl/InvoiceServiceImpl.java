package com.example.room.service.Impl;

import com.example.room.dto.BaseResponse;
import com.example.room.dto.PageResponse;
import com.example.room.dto.response.InvoiceResponse;
import com.example.room.exception.ForBiddenException;
import com.example.room.exception.ResourceNotFoundException;
import com.example.room.mapper.ContractMapper;
import com.example.room.mapper.InvoiceMapper;
import com.example.room.mapper.PaymentMapper;
import com.example.room.mapper.UserMapper;
import com.example.room.model.*;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
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
                .payment(payment)
                .contract(contract)
                .user(payment.getBooking().getUser())
                .build();

        // Lưu invoice trước để có ID (nếu cần)
        Invoice savedInvoice = invoiceRepository.save(newInvoice);
        payment.setInvoice(savedInvoice);
        paymentRepository.save(payment);

        // === TẠO PDF NGAY TẠI ĐÂY (KHÔNG ASYNC) ===
        try {
            // 1) Đường dẫn public lưu trong DB
            String publicPath = "/uploads/invoices/invoice-" + savedInvoice.getInvoiceNumber() + ".pdf";

            // 2) Đường dẫn tuyệt đối trên server
            String outputPath = System.getProperty("user.dir") + publicPath;

            // 3) Chuẩn bị context Thymeleaf
            Context context = new Context();
            context.setVariable("invoiceNumber", savedInvoice.getInvoiceNumber());
            context.setVariable("issueDate",
                    savedInvoice.getIssueDate() != null
                            ? savedInvoice.getIssueDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                            : "");
            context.setVariable("customerName", savedInvoice.getUser().getFullName());
            context.setVariable("customerEmail", savedInvoice.getUser().getEmail());
            context.setVariable("customerPhone", savedInvoice.getUser().getPhone());
            context.setVariable("roomName", savedInvoice.getContract().getBooking().getRoom().getName());
            context.setVariable("roomAddress", savedInvoice.getContract().getBooking().getRoom().getAddress());
            context.setVariable("paymentType", savedInvoice.getPayment().getPaymentType().name());
            context.setVariable("description", savedInvoice.getPayment().getDescription());
            context.setVariable("amount", savedInvoice.getPayment().getAmount());
            context.setVariable("paymentDate",
                    savedInvoice.getPayment().getPaymentDate() != null
                            ? savedInvoice.getPayment().getPaymentDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                            : "");
            context.setVariable("totalAmount", savedInvoice.getTotalAmount());

            // 4) Generate PDF
            pdfGeneratorService.generatePdf("invoice-template", context, outputPath);

            // 5) Kiểm tra file, log size cho chắc
            Path pdfPath = Path.of(outputPath);
            long size = Files.size(pdfPath);
            log.info("✅ Invoice PDF generated at {} (size = {} bytes)", outputPath, size);

            if (size == 0) {
                log.error("❌ Invoice PDF size is 0 bytes!");
            }

            // 6) Lưu PUBLIC path vào DB
            savedInvoice.setInvoiceFile(publicPath);
            invoiceRepository.save(savedInvoice);

            // 7) Gửi mail (nếu muốn)
            String subject = "Hóa đơn thanh toán: " + savedInvoice.getInvoiceNumber();
            String body = "Kính gửi " + savedInvoice.getUser().getFullName() + ",\n\n" +
                    "Vui lòng tìm hóa đơn thanh toán đính kèm.";
            emailService.sendEmailWithAttachment(savedInvoice.getUser().getEmail(), subject, body, outputPath);

        } catch (Exception e) {
            log.error("❌ Lỗi khi tạo PDF hóa đơn hoặc gửi mail: {}", e.getMessage(), e);
            // Không throw nữa cũng được, tùy bạn muốn fail toàn bộ hay chỉ log
        }

        InvoiceResponse response = invoiceMapper.toResponse(savedInvoice);
        return BaseResponse.<InvoiceResponse>builder()
                .code(201)
                .message("Invoice created successfully.")
                .data(response)
                .build();
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

        Page<Invoice> invoices = new PageImpl<>(Collections.emptyList(), pageable, 0);

        if (isAdmin) {
            invoices = invoiceRepository.findAll(pageable);
        } else if (isOwner) {
            // Lấy trực tiếp danh sách contractId của các contract liên quan tới phòng do owner sở hữu
            List<Long> contractIds = contractRepository.findIdsByBooking_Room_Owner_Id(currentUser.getId());
            if (contractIds == null || contractIds.isEmpty()) {
                invoices = new PageImpl<>(Collections.emptyList(), pageable, 0);
            } else {
                invoices = invoiceRepository.findByContract_IdIn(contractIds, pageable);
            }

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
        Payment payment = invoice.getPayment();
        User user = invoice.getUser();
        Contract contract = invoice.getContract();
        InvoiceResponse invoiceResponse = InvoiceResponse.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .issueDate(invoice.getIssueDate())
                .totalAmount(invoice.getTotalAmount())
                .payment(paymentMapper.toResponse(payment))
                .user(userMapper.toResponse(user))
                .contract(contractMapper.toResponse(contract))
                .build();
        return BaseResponse.<InvoiceResponse>builder()
                .code(200)
                .data(invoiceResponse)
                .message("Lấy thông tin hóa đơn thành công")
                .build();
    }

    @Override
    public ResponseEntity<byte[]> downloadInvoice(Long id) {
        // 1. Lấy hóa đơn từ DB
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn ID: " + id));

        // 2. Lấy đường dẫn file đã lưu, ví dụ: /uploads/invoices/invoice-10.pdf
        String publicPath = invoice.getInvoiceFile(); // đổi cho đúng tên field của bạn
        if (publicPath == null || publicPath.isBlank()) {
            throw new RuntimeException("Hóa đơn chưa có file PDF: invoiceFile = null");
        }

        // 3. Ghép thành path thật trên server
        String fullPath = System.getProperty("user.dir") + publicPath;

        try {
            // 4. Đọc file thành byte[]
            byte[] fileBytes = Files.readAllBytes(Path.of(fullPath));

            // 5. Header cho PDF
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(
                    ContentDisposition
                            .attachment()
                            .filename("invoice-" + id + ".pdf")
                            .build()
            );

            log.info("📄 Hóa đơn #{} đã được tải xuống từ {}", invoice.getInvoiceNumber(), fullPath);

            // 6. Trả về luôn ResponseEntity từ service
            return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);

        } catch (IOException e) {
            throw new RuntimeException("Không thể đọc file hóa đơn: " + fullPath, e);
        }
    }

    @Override
    public void cancelInvoice(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn ID: " + id));
        invoice.setDeleted(Boolean.TRUE);
        invoiceRepository.save(invoice);

        log.warn("⚠️ Hóa đơn #{} đã bị hủy bởi ADMIN.", invoice.getInvoiceNumber());
    }
}
