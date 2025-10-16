package com.example.room.service;

import com.example.room.dto.response.InvoiceResponse;
import com.example.room.model.Invoice; // <-- Thêm import
import com.example.room.model.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InvoiceService {
    Invoice createInvoiceRecord(Payment payment);

    void processAndSendInvoice(Long invoiceId);

    Page<InvoiceResponse> getInvoices(Pageable pageable);
    InvoiceResponse getInvoiceById(Long id);
    byte[] downloadInvoice(Long id);
    void sendInvoiceByEmail(Long id);
    void cancelInvoice(Long id);
}