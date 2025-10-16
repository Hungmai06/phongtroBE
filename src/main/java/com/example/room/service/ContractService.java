package com.example.room.service;

import com.example.room.dto.request.ContractUpdateRequest;
import com.example.room.dto.response.ContractResponse;
import com.example.room.model.Booking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface ContractService {

    Page<ContractResponse> getAllContracts(Pageable pageable);

    ContractResponse getContractById(long id);

    ContractResponse updateContract(long id, ContractUpdateRequest request);

    void deleteContract(long id);

    String downloadContractFile(long id);
    ContractResponse createContractFromBooking(Booking booking);
}