package com.example.room.service.Impl;

import com.example.room.dto.BaseResponse;
import com.example.room.dto.PageResponse;
import com.example.room.dto.request.ContractUpdateRequest;
import com.example.room.dto.request.ContractEmailRequest;
import com.example.room.dto.response.ContractResponse;
import com.example.room.exception.ForBiddenException;
import com.example.room.exception.ResourceNotFoundException;
import com.example.room.mapper.ContractMapper;
import com.example.room.model.Contract;
import com.example.room.model.Room;
import com.example.room.model.User;
import com.example.room.repository.BookingRepository;
import com.example.room.repository.ContractRepository;
import com.example.room.service.ContractService;
import com.example.room.service.EmailService;
import com.example.room.service.PdfGeneratorService;
import com.example.room.utils.Enums.ContractStatus;
import com.example.room.utils.Enums.RoleEnum;
import com.example.room.utils.Enums.RoomStatus;
import com.example.room.repository.RoomRepository;

import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.example.room.model.Booking; 
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import jakarta.mail.MessagingException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContractServiceImpl implements ContractService {

    private final ContractRepository contractRepository;
    private final ContractMapper contractMapper;
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final PdfGeneratorService pdfGeneratorService;
    private final EmailService emailService;

    @Override
    public PageResponse<ContractResponse> getAllContracts(Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
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

        return PageResponse.<ContractResponse>builder()
                .data(contracts.stream().map(contractMapper::toResponse).toList())
                .pageNumber(contracts.getNumber())
                .pageSize(contracts.getSize())
                .totalElements(contracts.getTotalElements())
                .totalPages(contracts.getTotalPages())
                .code(200)
                .message("Get all contracts successfully")
                .build();
    }

    @Override
    public BaseResponse<ContractResponse> getContractById(long id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with id: " + id));

        return BaseResponse.<ContractResponse>builder()
                .code(200)
                .message("Lấy thông tin hợp đồng thành công")
                .data(contractMapper.toResponse(contract))
                .build();
    }

    @Override
    @Transactional
    public BaseResponse<ContractResponse> updateContract(long id, ContractUpdateRequest request) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with id: " + id));
        
        if (request.getEndDate() != null) {
            contract.setEndDate(request.getEndDate());
        }

        if (request.getStatus() == ContractStatus.TERMINATED) {
            contract.setStatus(ContractStatus.TERMINATED);
        }

        Contract updatedContract = contractRepository.save(contract);
        return BaseResponse.<ContractResponse>builder()
                .code(200)
                .message("Cập nhật hợp đồng thành công")
                .data(contractMapper.toResponse(updatedContract))
                .build();
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
    public BaseResponse<ContractResponse> createContractFromBooking(Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));
        Room room = booking.getRoom();
        User renter = booking.getUser();
        User owner = room.getOwner();
        LocalDateTime contractStartDate = (booking.getStartDate() != null) ? booking.getStartDate() : LocalDateTime.now();
        Contract contract = Contract.builder()
                .booking(booking)
                .startDate(contractStartDate)
                .endDate(booking.getEndDate())
                .status(ContractStatus.ACTIVE)
                .build();

        // Save contract first so ID is generated
        Contract savedContract = contractRepository.save(contract);

        // Prepare Thymeleaf context for PDF
        Context context = new Context();
        context.setVariable("contract", savedContract);
        context.setVariable("owner", owner);
        context.setVariable("renter", renter);
        context.setVariable("room", room);

        // Generate output path using saved ID
        String outputPath = System.getProperty("user.dir") + "/uploads/contracts/contract-" + savedContract.getId() + ".pdf";
        pdfGeneratorService.generatePdf("contract-template", context, outputPath);

        // Lưu lại đường dẫn file (public path)
        String publicPath = "/uploads/contracts/contract-" + savedContract.getId() + ".pdf";
        savedContract.setContractFile(publicPath);
        contractRepository.save(savedContract);

        // Update room status
        room.setStatus(RoomStatus.RENTED);
        roomRepository.save(room);

        // Send email to renter and owner with attachment
        ContractEmailRequest emailReq = ContractEmailRequest.builder()
                .recipientName(renter.getFullName())
                .contractId(savedContract.getId())
                .startDate(savedContract.getStartDate())
                .endDate(savedContract.getEndDate())
                .roomName(room.getName())
                .roomAddress(room.getAddress())
                .price(room.getPrice())
                .ownerName(owner.getFullName())
                .ownerEmail(owner.getEmail())
                .ownerPhone(owner.getPhone())
                .renterName(renter.getFullName())
                .renterEmail(renter.getEmail())
                .renterPhone(renter.getPhone())
                .contractUrl(publicPath)
                .year(LocalDateTime.now().getYear())
                .build();

        try {
            emailService.sendContractInfoWithAttachment(emailReq, renter.getEmail(), outputPath);
        } catch (MessagingException e) {
            log.error("Failed to send contract email to renter {}: {}", renter.getEmail(), e.getMessage());
        }


        emailReq.setRecipientName(owner.getFullName());
        try {
            emailService.sendContractInfoWithAttachment(emailReq, owner.getEmail(), outputPath);
        } catch (MessagingException e) {
            log.error("Failed to send contract email to owner {}: {}", owner.getEmail(), e.getMessage());
        }

        return BaseResponse.<ContractResponse>builder()
                .code(200)
                .message("Tạo hợp đồng hợp đồng thành công")
                .data(contractMapper.toResponse(savedContract))
                .build();
    }

}