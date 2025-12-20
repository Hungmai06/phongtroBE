package com.example.room.service.Impl;

import com.example.room.dto.BaseResponse;
import com.example.room.dto.PageResponse;
import com.example.room.dto.events.ContractMailEvent;
import com.example.room.dto.request.ContractUpdateRequest;
import com.example.room.dto.request.ContractEmailRequest;
import com.example.room.dto.response.ContractResponse;
import com.example.room.dto.response.RoomTenantResponse;
import com.example.room.exception.ForBiddenException;
import com.example.room.exception.ResourceNotFoundException;
import com.example.room.mapper.ContractMapper;
import com.example.room.model.*;
import com.example.room.repository.BookingRepository;
import com.example.room.repository.ContractRepository;
import com.example.room.repository.PaymentRepository;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    private final PaymentRepository paymentRepository;
   private final ApplicationEventPublisher publisher;
    @Override
    public PageResponse<ContractEmailRequest> getAllContracts(Integer page, Integer size) {
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
        List<ContractEmailRequest> contractEmailRequests = new ArrayList<>();
        for (Contract contract : contracts.getContent()) {
            Room room = contract.getBooking().getRoom();
            User renter = contract.getBooking().getUser();
            User owner = room.getOwner();

            ContractEmailRequest emailReq = ContractEmailRequest.builder()
                    .recipientName(renter.getFullName())
                    .contractId(contract.getId())
                    .startDate(contract.getStartDate())
                    .endDate(contract.getEndDate())
                    .roomName(room.getName())
                    .roomAddress(room.getAddress())
                    .price(room.getPrice())
                    .ownerName(owner.getFullName())
                    .ownerEmail(owner.getEmail())
                    .ownerPhone(owner.getPhone())
                    .renterName(renter.getFullName())
                    .renterEmail(renter.getEmail())
                    .renterPhone(renter.getPhone())
                    .year(LocalDateTime.now().getYear())
                    .build();
            contractEmailRequests.add(emailReq);
        }

        return PageResponse.<ContractEmailRequest>builder()
                .data(contractEmailRequests)
                .pageNumber(contracts.getNumber())
                .pageSize(contracts.getSize())
                .totalElements(contracts.getTotalElements())
                .totalPages(contracts.getTotalPages())
                .code(200)
                .message("Get all contracts successfully")
                .build();
    }

    @Override
    public BaseResponse<ContractEmailRequest> getContractById(long id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with id: " + id));
        Room room = contract.getBooking().getRoom();
        User renter = contract.getBooking().getUser();
        User owner = room.getOwner();

        ContractEmailRequest emailReq = ContractEmailRequest.builder()
                .recipientName(renter.getFullName())
                .contractId(contract.getId())
                .startDate(contract.getStartDate())
                .endDate(contract.getEndDate())
                .roomName(room.getName())
                .roomAddress(room.getAddress())
                .price(room.getPrice())
                .ownerName(owner.getFullName())
                .ownerEmail(owner.getEmail())
                .ownerPhone(owner.getPhone())
                .renterName(renter.getFullName())
                .renterEmail(renter.getEmail())
                .renterPhone(renter.getPhone())
                .year(LocalDateTime.now().getYear())
                .build();

        return BaseResponse.<ContractEmailRequest>builder()
                .code(200)
                .message("Lấy thông tin hợp đồng thành công")
                .data(emailReq)
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
        if( request.getStatus() == ContractStatus.EXPIRED) {
            contract.setStatus(ContractStatus.EXPIRED);
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
    public ResponseEntity<byte[]> downloadContract(long id) {

        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with id: " + id));

        String publicPath = contract.getContractFile(); // "/uploads/contracts/contract-1.pdf"
        String fullPath = System.getProperty("user.dir") + publicPath;

        try {
            byte[] fileBytes = Files.readAllBytes(Path.of(fullPath));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);

            // 🔥 Dùng ContentDisposition chuẩn để tránh lỗi tải file trên Chrome/Edge
            headers.setContentDisposition(
                    ContentDisposition
                            .attachment()
                            .filename("contract-" + id + ".pdf")
                            .build()
            );

            return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);

        } catch (IOException e) {
            throw new RuntimeException("Cannot read PDF file: " + fullPath, e);
        }
    }
    @Override
    @Transactional
    public BaseResponse<ContractEmailRequest> createContractPayment(Long paymentId, LocalDate startDate, LocalDate endDate) {
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(
                ()-> new ResourceNotFoundException("Payment not found with id: " + paymentId)
        );
        Booking booking = bookingRepository.findById(payment.getBooking().getId()).orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + payment.getBooking().getId()));
        Room room = booking.getRoom();
        User renter = booking.getUser();
        User owner = room.getOwner();
        Contract contract = Contract.builder()
                .booking(booking)
                .status(ContractStatus.ACTIVE)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        // Save contract first so ID is generated
        Contract savedContract = contractRepository.save(contract);
        payment.setContract(savedContract);
        paymentRepository.save(payment);
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

        ContractMailEvent event = new ContractMailEvent(this,emailReq);
        publisher.publishEvent(event);
//        try {
//            emailService.sendContractInfoWithAttachment(emailReq, renter.getEmail(), outputPath);
//        } catch (MessagingException e) {
//            log.error("Failed to send contract email to renter {}: {}", renter.getEmail(), e.getMessage());
//        }


        emailReq.setRecipientName(owner.getFullName());
//        try {
//            emailService.sendContractInfoWithAttachment(emailReq, owner.getEmail(), outputPath);
//        } catch (MessagingException e) {
//            log.error("Failed to send contract email to owner {}: {}", owner.getEmail(), e.getMessage());
//        }

        return BaseResponse.<ContractEmailRequest>builder()
                .code(200)
                .message("Tạo hợp đồng hợp đồng thành công")
                .data(emailReq)
                .build();
    }
    @Override
    public PageResponse<RoomTenantResponse> getCurrentTenantsForOwner(Integer page, Integer size) {

        User currentUser = (User) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        boolean isOwner = currentUser.getRole().getName().name().equals(RoleEnum.OWNER.name());
        if (!isOwner) {
            throw new ForBiddenException("Only OWNER can view current tenants");
        }

        Pageable pageable = PageRequest.of(page, size);
        LocalDateTime now = LocalDateTime.now();

        // 🔥 1) Lấy tất cả contract ACTIVE của owner
        List<Contract> activeContracts = contractRepository.findActiveContractsByOwner(
                currentUser.getId(),
                ContractStatus.ACTIVE,
                now
        );

        // 🔥 2) Convert sang DTO
        List<RoomTenantResponse> dtoList = new ArrayList<>();

        for (Contract contract : activeContracts) {
            Booking booking = contract.getBooking();
            Room room = booking.getRoom();
            User renter = booking.getUser();
            if(room.getStatus().equals(RoomStatus.RENTED)) {
                RoomTenantResponse dto = RoomTenantResponse.builder()
                        .roomId(room.getId())
                        .roomName(room.getName())
                        .roomAddress(room.getAddress())
                        .roomStatus(room.getStatus().name())
                        .renterId(renter.getId())
                        .renterName(renter.getFullName())
                        .renterEmail(renter.getEmail())
                        .renterPhone(renter.getPhone())
                        .contractId(contract.getId())
                        .contractStartDate(contract.getStartDate())
                        .contractEndDate(contract.getEndDate())
                        .build();

                dtoList.add(dto);
            }
        }

        // 🔥 3) Tự phân trang bằng tay vì query trả về List
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), dtoList.size());

        List<RoomTenantResponse> pageContent =
                start >= dtoList.size() ? List.of() : dtoList.subList(start, end);

        return PageResponse.<RoomTenantResponse>builder()
                .code(200)
                .message("Lấy danh sách phòng và người thuê hiện tại thành công")
                .data(pageContent)
                .pageNumber(page)
                .pageSize(size)
                .totalElements(dtoList.size())
                .totalPages((int) Math.ceil((double) dtoList.size() / size))
                .build();
    }
}