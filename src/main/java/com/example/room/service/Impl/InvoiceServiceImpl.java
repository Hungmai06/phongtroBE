package com.example.room.service.Impl;

import com.example.room.dto.response.InvoiceResponse;
import com.example.room.exception.ResourceNotFoundException;
import com.example.room.mapper.InvoiceMapper;
import com.example.room.model.Contract;
import com.example.room.model.Invoice;
import com.example.room.model.Payment;
import com.example.room.repository.ContractRepository;
import com.example.room.repository.InvoiceRepository;
import com.example.room.service.InvoiceService;
import com.example.room.utils.Enums.ContractStatus;
import com.example.room.utils.Enums.InvoiceStatus;
import com.example.room.utils.Enums.RoleEnum;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import com.example.room.exception.ForBiddenException;
import com.example.room.model.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final ContractRepository contractRepository;
    private final InvoiceMapper invoiceMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Invoice createInvoiceRecord(Payment payment) {
        // --- BẮT ĐẦU SỬA ĐỔI LOGIC ---

        // Bước 1: Kiểm tra xem hóa đơn đã tồn tại cho thanh toán này chưa
        if (invoiceRepository.existsByPaymentId(payment.getId())) {
            log.warn("Hóa đơn cho payment ID {} đã tồn tại. Bỏ qua việc tạo mới.", payment.getId());
            return null; 
        }

        // Bước 2: Tìm hợp đồng (không bắt buộc phải ACTIVE)
        Optional<Contract> contractOptional = contractRepository.findByBooking_Id(payment.getBooking().getId())
                .stream()
                .findFirst(); // Lấy hợp đồng bất kỳ liên quan đến booking

        Invoice invoice = Invoice.builder()
                .invoiceNumber(generateInvoiceNumber())
                .issueDate(LocalDateTime.now())
                .totalAmount(payment.getAmount())
                .status(InvoiceStatus.CREATED)
                .payment(payment)
                .contract(contractOptional.orElse(null)) // Gán hợp đồng nếu tìm thấy, ngược lại gán null
                .user(payment.getBooking().getUser())
                .build();

        invoiceRepository.save(invoice);
        log.info("✅ Hóa đơn mới được tạo: {}", invoice.getInvoiceNumber());

        // Gửi hóa đơn bất đồng bộ
        processAndSendInvoice(invoice.getId());
        return invoice;
    }

    @Async
    @Override
    public void processAndSendInvoice(Long invoiceId) {
        try {
            // Tìm invoice ngay lập tức thay vì đợi
            Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found for async processing: " + invoiceId));

            log.info("Bắt đầu xử lý và gửi hóa đơn #{}...", invoice.getInvoiceNumber());
            Thread.sleep(3000); // Giả lập thời gian gửi mail hoặc tạo PDF

            // TODO: Logic tạo PDF và gửi Email
            
            invoice.setStatus(InvoiceStatus.SENT);
            invoiceRepository.save(invoice);
            log.info("📨 Hóa đơn #{} đã được gửi thành công đến người dùng.", invoice.getInvoiceNumber());

        } catch (InterruptedException e) {
            log.error("Luồng xử lý hóa đơn bị gián đoạn.", e);
            Thread.currentThread().interrupt();
        } catch (ResourceNotFoundException e) {
            log.error("Không thể xử lý hóa đơn không đồng bộ: {}", e.getMessage());
        }
    }

    private String generateInvoiceNumber() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int randomNum = ThreadLocalRandom.current().nextInt(1000, 10000);
        return "INV-" + datePart + "-" + randomNum;
    }

    @Override
    public Page<InvoiceResponse> getInvoices(Pageable pageable) {
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

        return invoices.map(invoiceMapper::toResponse);
    }

    @Override
    public InvoiceResponse getInvoiceById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn ID: " + id));

        return invoiceMapper.toResponse(invoice);
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
