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

    @PreAuthorize("hasAnyAuthority('ADMIN', 'OWNER', 'RENTER')")
    @GetMapping
    public ResponseEntity<PageResponse<InvoiceResponse>> getInvoices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<InvoiceResponse> invoicePage = invoiceService.getInvoices(pageable);

        PageResponse<InvoiceResponse> response = PageResponse.<InvoiceResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy danh sách hóa đơn thành công")
                .data(invoicePage.getContent())
                .build();

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'OWNER', 'RENTER') and @securityService.canAccessInvoice(#id)")
    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<InvoiceResponse>> getInvoiceById(@PathVariable Long id) {
        InvoiceResponse invoice = invoiceService.getInvoiceById(id);
        BaseResponse<InvoiceResponse> response = BaseResponse.<InvoiceResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy thông tin hóa đơn thành công")
                .data(invoice)
                .build();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyAuthority('OWNER', 'RENTER') and @securityService.canAccessInvoice(#id)")
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable Long id) {
        byte[] pdfBytes = invoiceService.downloadInvoice(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "invoice-" + id + ".pdf");
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyAuthority('OWNER', 'ADMIN') and @securityService.canAccessInvoice(#id)")
    @PostMapping("/send/{id}")
    public ResponseEntity<BaseResponse<Object>> sendInvoice(@PathVariable Long id) {
        invoiceService.sendInvoiceByEmail(id);
        BaseResponse<Object> response = BaseResponse.builder()
                .code(HttpStatus.OK.value())
                .message("Đã gửi lại hóa đơn thành công")
                .build();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAuthority('ADMIN') and @securityService.canAccessInvoice(#id)")
    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Object>> cancelInvoice(@PathVariable Long id) {
        invoiceService.cancelInvoice(id);
        BaseResponse<Object> response = BaseResponse.builder()
                .code(HttpStatus.OK.value())
                .message("Hủy hóa đơn thành công")
                .build();
        return ResponseEntity.ok(response);
    }
}