package com.example.room.service.Impl;

import com.example.room.dto.BaseResponse;
import com.example.room.dto.request.BankAccountRequest;
import com.example.room.dto.response.BankAccountResponse;
import com.example.room.exception.InvalidDataException;
import com.example.room.exception.ResourceNotFoundException;
import com.example.room.mapper.BankAccountMapper;
import com.example.room.model.BankAccount;
import com.example.room.model.User;
import com.example.room.repository.BankAccountRepository;
import com.example.room.repository.UserRepository;
import com.example.room.service.BankAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankAccountServiceImpl implements BankAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final UserRepository userRepository;
    private final BankAccountMapper bankAccountMapper;

    @Override
    @Transactional
    public BaseResponse<BankAccountResponse> create(BankAccountRequest request) {

        Optional<BankAccount> existed = bankAccountRepository.findByAccountNumber(request.getAccountNumber());
        if (existed.isPresent()) {
            throw new InvalidDataException("Số tài khoản đã tồn tại");
        }

        BankAccount entity = BankAccount.builder()
                .bankCode(request.getBankCode())
                .bankName(request.getBankName())
                .accountNumber(request.getAccountNumber())
                .accountName(request.getAccountName())
                .build();

        if (request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user với id: " + request.getUserId()));
            entity.setUser(user);
        }

        BankAccount saved = bankAccountRepository.save(entity);
        return BaseResponse.<BankAccountResponse>builder()
                .code(201)
                .message("Tạo tài khoản ngân hàng thành công")
                .data(bankAccountMapper.toResponse(saved))
                .build();
    }

    @Override
    public BaseResponse<BankAccountResponse> getBankAccountById(Long id) {
        BankAccount account = bankAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với id: " + id));

        return BaseResponse.<BankAccountResponse>builder()
                .code(200)
                .message("Lấy thông tin tài khoản thành công")
                .data(bankAccountMapper.toResponse(account))
                .build();
    }

    @Override
    @Transactional
    public BaseResponse<BankAccountResponse> update(Long id, BankAccountRequest request) {
        BankAccount account = bankAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với id: " + id));

        if (request.getAccountNumber() != null && !request.getAccountNumber().isBlank()) {
            account.setAccountNumber(request.getAccountNumber());
        }

        if (request.getBankCode() != null) account.setBankCode(request.getBankCode());
        if (request.getBankName() != null) account.setBankName(request.getBankName());
        if (request.getAccountName() != null) account.setAccountName(request.getAccountName());

        BankAccount updated = bankAccountRepository.save(account);
        return BaseResponse.<BankAccountResponse>builder()
                .code(200)
                .message("Cập nhật tài khoản thành công")
                .data(bankAccountMapper.toResponse(updated))
                .build();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        BankAccount account = bankAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với id: " + id));
        account.setDeleted(Boolean.TRUE);
        bankAccountRepository.save(account);
    }

}
