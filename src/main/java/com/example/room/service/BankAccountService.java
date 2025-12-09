package com.example.room.service;

import com.example.room.dto.BaseResponse;
import com.example.room.dto.request.BankAccountRequest;
import com.example.room.dto.response.BankAccountResponse;

public interface BankAccountService {
    BaseResponse<BankAccountResponse> create(BankAccountRequest request);

    BaseResponse<BankAccountResponse> getBankAccountById(Long id);

    BaseResponse<BankAccountResponse> update(Long id, BankAccountRequest request) ;

    BaseResponse<BankAccountResponse> getBankAccountByUser();
    void delete(Long id);
}
