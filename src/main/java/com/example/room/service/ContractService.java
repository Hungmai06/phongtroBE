package com.example.room.service;

import com.example.room.dto.BaseResponse;
import com.example.room.dto.PageResponse;
import com.example.room.dto.request.ContractEmailRequest;
import com.example.room.dto.request.ContractUpdateRequest;
import com.example.room.dto.response.ContractResponse;
import com.example.room.model.Booking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface ContractService {

    PageResponse<ContractEmailRequest> getAllContracts(Integer page, Integer size);

    BaseResponse<ContractEmailRequest> getContractById(long id);

    BaseResponse<ContractResponse> updateContract(long id, ContractUpdateRequest request);

    void deleteContract(long id);

    String downloadContractFile(long id);
    BaseResponse<ContractEmailRequest> createContractFromBooking(Long bookingId);
}