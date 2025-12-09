package com.example.room.service;

import com.example.room.dto.BaseResponse;
import com.example.room.dto.PageResponse;
import com.example.room.dto.response.InvoiceResponse;
import org.springframework.http.ResponseEntity;

public interface InvoiceService {
    BaseResponse<InvoiceResponse> createInvoiceRecord(Long paymentId);
    PageResponse<InvoiceResponse> getInvoices(Integer page, Integer size);
    BaseResponse<InvoiceResponse> getInvoiceById(Long id);
    ResponseEntity<byte[]> downloadInvoice(Long id);
    void cancelInvoice(Long id);
}