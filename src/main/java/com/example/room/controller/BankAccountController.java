package com.example.room.controller;

import com.example.room.dto.BaseResponse;
import com.example.room.dto.request.BankAccountRequest;
import com.example.room.dto.response.BankAccountResponse;
import com.example.room.service.BankAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bank-accounts")
@Tag(name = "API BANK ACCOUNT", description = "API cho quản lý tài khoản ngân hàng của chủ trọ")
public class BankAccountController {

    private final BankAccountService bankAccountService;

    @PostMapping("")
    @Operation(summary = "Tạo tài khoản ngân hàng")
    public BaseResponse<BankAccountResponse> create(@Valid @RequestBody BankAccountRequest request) {
        return bankAccountService.create(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy thông tin tài khoản theo id")
    public BaseResponse<BankAccountResponse> getById(@PathVariable Long id) {
        return bankAccountService.getBankAccountById(id);
    }
    @GetMapping
    @Operation(summary = "Lấy thông tin tài khoản của chủ trọ")
    public BaseResponse<BankAccountResponse> getBankAccountByUser() {
        return bankAccountService.getBankAccountByUser();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật tài khoản ngân hàng")
    public BaseResponse<BankAccountResponse> update(@PathVariable Long id, @Valid @RequestBody BankAccountRequest request) {
        return bankAccountService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa tài khoản ngân hàng (soft-delete)")
    public BaseResponse<String> delete(@PathVariable Long id) {
        bankAccountService.delete(id);
        return BaseResponse.<String>builder().code(200).message("Xóa tài khoản thành công").data(null).build();
    }
}
