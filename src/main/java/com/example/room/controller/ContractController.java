package com.example.room.controller;

import com.example.room.dto.BaseResponse;
import com.example.room.dto.request.ContractUpdateRequest;
import com.example.room.dto.response.ContractResponse;
import com.example.room.service.ContractService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<BaseResponse> getContracts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<ContractResponse> contractsPage = contractService.getAllContracts(PageRequest.of(page, size));

        return ResponseEntity.ok(
                BaseResponse.builder()
                        .code(HttpStatus.OK.value())
                        .message("Lấy danh sách hợp đồng thành công")
                        .data(contractsPage)
                        .build()
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'OWNER', 'RENTER') and @securityService.canAccessContract(#id)")
    @Operation(summary = "Lấy chi tiết hợp đồng theo ID (Admin/Owner/Renter)")
    public ResponseEntity<BaseResponse> getContractById(@PathVariable long id) {
        ContractResponse contract = contractService.getContractById(id);
        return ResponseEntity.ok(
                BaseResponse.builder()
                        .code(HttpStatus.OK.value())
                        .message("Lấy chi tiết hợp đồng thành công")
                        .data(contract)
                        .build()
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'OWNER') and @securityService.canAccessContract(#id)")
    @Operation(summary = "Cập nhật hợp đồng (chỉ Owner hoặc Admin được phép cập nhật)")
    public ResponseEntity<BaseResponse> updateContract(@PathVariable long id, @RequestBody ContractUpdateRequest request) {
        ContractResponse updatedContract = contractService.updateContract(id, request);
        return ResponseEntity.ok(
                BaseResponse.builder()
                        .code(HttpStatus.OK.value())
                        .message("Cập nhật hợp đồng thành công")
                        .data(updatedContract)
                        .build()
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'OWNER') and @securityService.canAccessContract(#id)")
    @Operation(summary = "Xóa hợp đồng (chỉ Owner hoặc Admin được phép xóa)")
    public ResponseEntity<BaseResponse> deleteContract(@PathVariable long id) {
        contractService.deleteContract(id);
        return ResponseEntity.ok(
                BaseResponse.builder()
                        .code(HttpStatus.OK.value())
                        .message("Xóa hợp đồng thành công")
                        .build()
        );
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'OWNER', 'RENTER') and @securityService.canAccessContract(#id)")
    @Operation(summary = "Tải file hợp đồng (chỉ Admin/Owner/Renter của hợp đồng được phép tải)")
    public ResponseEntity<?> downloadContractFile(@PathVariable long id) {
        String fileUrl = contractService.downloadContractFile(id);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, fileUrl)
                .build();
    }
}
