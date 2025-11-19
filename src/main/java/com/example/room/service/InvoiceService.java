package com.example.room.service;

import com.example.room.dto.BaseResponse;
import com.example.room.dto.PageResponse;
import com.example.room.dto.response.InvoiceResponse;

public interface InvoiceService {
    BaseResponse<InvoiceResponse> createInvoiceRecord(Long paymentId);
    void processAndSendInvoice(Long invoiceId);
    PageResponse<InvoiceResponse> getInvoices(Integer page, Integer size);
    BaseResponse<InvoiceResponse> getInvoiceById(Long id);
    byte[] downloadInvoice(Long id);
    void sendInvoiceByEmail(Long id);
    void cancelInvoice(Long id);
}