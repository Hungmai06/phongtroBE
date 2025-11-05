package com.example.room.controller;

import com.example.room.dto.BaseResponse;
import com.example.room.dto.PageResponse;
import com.example.room.dto.request.ContractUpdateRequest;
import com.example.room.dto.response.ContractResponse;
import com.example.room.service.ContractService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
@Tag(name = "API CONTRACT", description = "API quản lý hợp đồng thuê trọ")
public class ContractController {

    private final ContractService contractService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'OWNER', 'RENTER')")
    @Operation(summary = "Lấy danh sách tất cả hợp đồng (Admin xem tất cả, Owner/Renter chỉ xem hợp đồng của mình)")
    public PageResponse<ContractResponse> getContracts(
            @RequestParam int page,
            @RequestParam int size) {
        return contractService.getAllContracts(page, size);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'OWNER', 'RENTER') and @securityService.canAccessContract(#id)")
    @Operation(summary = "Lấy chi tiết hợp đồng theo ID (Admin/Owner/Renter)")
    public BaseResponse<ContractResponse> getContractById(@PathVariable long id) {
        return contractService.getContractById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'OWNER') and @securityService.canAccessContract(#id)")
    @Operation(summary = "Cập nhật hợp đồng (chỉ Owner hoặc Admin được phép cập nhật)")
    public BaseResponse<ContractResponse>  updateContract(@PathVariable long id, @RequestBody ContractUpdateRequest request) {
        return contractService.updateContract(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'OWNER') and @securityService.canAccessContract(#id)")
    @Operation(summary = "Xóa hợp đồng (chỉ Owner hoặc Admin được phép xóa)")
    public void deleteContract(@PathVariable long id) {
        contractService.deleteContract(id);

    }

    @GetMapping("/{id}/download")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'OWNER', 'RENTER') and @securityService.canAccessContract(#id)")
    @Operation(summary = "Tải file hợp đồng (chỉ Admin/Owner/Renter của hợp đồng được phép tải)")
    public BaseResponse<?> downloadContractFile(@PathVariable long id) {
        String fileUrl = contractService.downloadContractFile(id);
        return BaseResponse.builder()
                .code(200)
                .message("Download Successfully")
                .data(fileUrl)
                .build();
    }
}
