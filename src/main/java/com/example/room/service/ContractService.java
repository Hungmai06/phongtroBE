package com.example.room.service;

import com.example.room.dto.BaseResponse;
import com.example.room.dto.PageResponse;
import com.example.room.dto.request.ContractUpdateRequest;
import com.example.room.dto.response.ContractResponse;
import com.example.room.model.Booking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface ContractService {

    PageResponse<ContractResponse> getAllContracts(Integer page, Integer size);

    BaseResponse<ContractResponse> getContractById(long id);

    BaseResponse<ContractResponse> updateContract(long id, ContractUpdateRequest request);

    void deleteContract(long id);

    String downloadContractFile(long id);
    BaseResponse<ContractResponse> createContractFromBooking(Long bookingId);
}