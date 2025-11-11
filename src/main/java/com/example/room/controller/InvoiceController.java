package com.example.room.controller;

import com.example.room.dto.BaseResponse;
import com.example.room.dto.PageResponse;
import com.example.room.dto.response.InvoiceResponse;
import com.example.room.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'RENTER')")
    @GetMapping
    public PageResponse<InvoiceResponse> getInvoices(
            @RequestParam int page,
            @RequestParam int size
    ) {
      return invoiceService.getInvoices(page, size);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'RENTER') and @securityService.canAccessInvoice(#id)")
    @GetMapping("/{id}")
    public BaseResponse<InvoiceResponse> getInvoiceById(@PathVariable Long id) {
      return invoiceService.getInvoiceById(id);
    }

    @PreAuthorize("hasAnyRole('OWNER', 'RENTER') and @securityService.canAccessInvoice(#id)")
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable Long id) {
        byte[] pdfBytes = invoiceService.downloadInvoice(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "invoice-" + id + ".pdf");
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN') and @securityService.canAccessInvoice(#id)")
    @PostMapping("/send/{id}")
    public BaseResponse<?> sendInvoice(@PathVariable Long id) {
        invoiceService.sendInvoiceByEmail(id);
        return BaseResponse.builder()
                .code(200)
                .message("Đã gửi lại hóa đơn thành công")
                .build();
    }

    @PreAuthorize("hasRole('ADMIN') and @securityService.canAccessInvoice(#id)")
    @DeleteMapping("/{id}")
    public BaseResponse<Object> cancelInvoice(@PathVariable Long id) {
        invoiceService.cancelInvoice(id);
        return BaseResponse.builder()
                .code(200)
                .message("Hủy hóa đơn thành công")
                .build();
    }
}