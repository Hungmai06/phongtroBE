package com.example.room.service;

import com.example.room.dto.BaseResponse;
import com.example.room.dto.PageResponse;
import com.example.room.dto.request.ContractEmailRequest;
import com.example.room.dto.request.ContractUpdateRequest;
import com.example.room.dto.response.ContractResponse;
import com.example.room.dto.response.RoomTenantResponse;
import com.example.room.model.Booking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;


public interface ContractService {

    PageResponse<ContractEmailRequest> getAllContracts(Integer page, Integer size);

    BaseResponse<ContractEmailRequest> getContractById(long id);

    BaseResponse<ContractResponse> updateContract(long id, ContractUpdateRequest request);

    void deleteContract(long id);

    ResponseEntity<byte[]> downloadContract(long id);
    BaseResponse<ContractEmailRequest> createContractPayment(Long bookingId, LocalDate startDate, LocalDate endDate);
    PageResponse<RoomTenantResponse> getCurrentTenantsForOwner(Integer page, Integer size);
}