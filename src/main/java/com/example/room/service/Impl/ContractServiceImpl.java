package com.example.room.service.Impl;

import com.example.room.dto.request.ContractUpdateRequest;
import com.example.room.dto.response.ContractResponse;
import com.example.room.exception.ForBiddenException;
import com.example.room.exception.ResourceNotFoundException;
import com.example.room.mapper.ContractMapper;
import com.example.room.model.Contract;
import com.example.room.model.Room;
import com.example.room.model.User;
import com.example.room.repository.ContractRepository;
import com.example.room.service.ContractService;
import com.example.room.utils.Enums.ContractStatus;
import com.example.room.utils.Enums.RoleEnum;
import com.example.room.utils.Enums.RoomStatus;
import com.example.room.repository.RoomRepository;

import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.example.room.model.Booking; 
import org.springframework.security.access.AccessDeniedException;
import com.example.room.utils.Enums.ContractStatus; 
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {

    private final ContractRepository contractRepository;
    private final ContractMapper contractMapper;
    private final RoomRepository roomRepository;

    @Override
    public Page<ContractResponse> getAllContracts(Pageable pageable) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        boolean isAdmin = currentUser.getRole().getName().name().equals(RoleEnum.ADMIN.name());
        boolean isOwner = currentUser.getRole().getName().name().equals(RoleEnum.OWNER.name());
        boolean isRenter = currentUser.getRole().getName().name().equals(RoleEnum.RENTER.name());

        Page<Contract> contracts;

        if (isAdmin) {
            // ADMIN: Lấy tất cả hợp đồng
            contracts = contractRepository.findAll(pageable);
        } else if (isOwner) {
            // OWNER: Lấy hợp đồng liên quan đến phòng của họ
            contracts = contractRepository.findByBooking_Room_Owner_Id(currentUser.getId(), pageable);
        } else if (isRenter) {
            // RENTER: Lấy hợp đồng của chính họ
            contracts = contractRepository.findByBooking_User_Id(currentUser.getId(), pageable);
        } else {
            // Vai trò không xác định, không trả về gì
            throw new ForBiddenException("Your role is not authorized to access this resource.");
        }

        return contracts.map(contractMapper::toResponse);
    }

    @Override
    public ContractResponse getContractById(long id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with id: " + id));

        return contractMapper.toResponse(contract);
    }

    @Override
    @Transactional
    public ContractResponse updateContract(long id, ContractUpdateRequest request) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with id: " + id));
        
        if (request.getEndDate() != null) {
            contract.setEndDate(request.getEndDate());
        }

        if (request.getStatus() == ContractStatus.TERMINATED) {
            contract.setStatus(ContractStatus.TERMINATED);
        }

        Contract updatedContract = contractRepository.save(contract);
        return contractMapper.toResponse(updatedContract);
    }

    @Override
    @Transactional
    public void deleteContract(long id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with id: " + id));

        contract.setDeleted(true);
        contractRepository.save(contract);
    }

    @Override
    public String downloadContractFile(long id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with id: " + id));
        return contract.getContractFile();
    }
    @Override
    @Transactional
    public ContractResponse createContractFromBooking(Booking booking) {
        // TODO: Tích hợp logic sinh file PDF và upload lên cloud ở đây
        // Tạm thời giả lập một đường dẫn file
        String pdfFileUrl = "/uploads/contracts/contract-booking-" + booking.getId() + ".pdf";

        Contract newContract = Contract.builder()
                .booking(booking)
                .startDate(booking.getStartDate().atStartOfDay()) 
                .endDate(booking.getEndDate().atStartOfDay())
                .status(ContractStatus.ACTIVE)
                .contractFile(pdfFileUrl)
                .build();

        Contract savedContract = contractRepository.save(newContract);
        Room room = booking.getRoom();
        room.setStatus(RoomStatus.RENTED);
        roomRepository.save(room);

        // TODO: Gửi email cho Owner và Renter kèm file PDF

        return contractMapper.toResponse(savedContract);
    }

}