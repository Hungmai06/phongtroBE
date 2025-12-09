package com.example.room.service.Impl;

import com.example.room.dto.BaseResponse;
import com.example.room.dto.PageResponse;
import com.example.room.dto.request.OwnerRequestRequest;
import com.example.room.dto.request.OwnerRequestUpdate;
import com.example.room.dto.response.OwnerRequestResponse;
import com.example.room.exception.InvalidDataException;
import com.example.room.exception.ResourceNotFoundException;
import com.example.room.mapper.OwnerRequestMapper;
import com.example.room.model.OwnerRequest;
import com.example.room.model.Role;
import com.example.room.model.User;
import com.example.room.repository.OwnerRequestRepository;
import com.example.room.repository.RoleRepository;
import com.example.room.repository.UserRepository;
import com.example.room.service.EmailService;
import com.example.room.service.OwnerRequestService;
import com.example.room.utils.Enums.OwnerRequestStatus;
import com.example.room.utils.Enums.RoleEnum;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OwnerRequestServiceImpl implements OwnerRequestService {

    private final OwnerRequestRepository ownerRequestRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OwnerRequestMapper ownerRequestMapper;
    private final EmailService emailService;

    @Override
    @Transactional
    public BaseResponse<OwnerRequestResponse> createRequest(OwnerRequestRequest dto) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        String currentRole = user.getRole().getName().toString();
        if ("OWNER".equals(currentRole) || "ADMIN".equals(currentRole)) {
            throw new InvalidDataException("Bạn đã là chủ nhà hoặc quản trị viên");
        }

        boolean existsPending = ownerRequestRepository
                .existsByUser_IdAndStatus(user.getId(), OwnerRequestStatus.PENDING);
        if (existsPending) {
          throw new InvalidDataException("Bạn đã có một yêu cầu đang chờ xử lý");
        }

        OwnerRequest req = OwnerRequest.builder()
                .reason(dto.getReason())
                .status(OwnerRequestStatus.PENDING)
                .user(user)
                .build();
        OwnerRequest saved = ownerRequestRepository.save(req);
        OwnerRequestResponse response = ownerRequestMapper.toResponse(saved);

        return BaseResponse.<OwnerRequestResponse>builder()
                .code(201)
                .message("tạo yêu cầu thành công")
                .data(response)
                .build();
    }

    @Override
    @Transactional
    public BaseResponse<OwnerRequestResponse> handle(OwnerRequestUpdate request) throws MessagingException {
        OwnerRequest req = ownerRequestRepository.findById(request.getRequestId())
                .orElseThrow(() -> new ResourceNotFoundException("Yêu cầu không tồn tại"));

        User user = req.getUser();
        Role ownerRole = roleRepository.findByName(RoleEnum.OWNER)
                .orElseThrow(() -> new RuntimeException("Role OWNER không tồn tại"));

        if(request.getStatus().equals(OwnerRequestStatus.APPROVED.toString())) {
            user.setRole(ownerRole);
            userRepository.save(user);

            req.setStatus(OwnerRequestStatus.APPROVED);
            ownerRequestRepository.save(req);
            String description = "Yêu cầu làm chủ nhà của bạn đã được phê duyệt.";
            emailService.sendRegisterSuccessEmail(user.getEmail(), user.getFullName(),description);
        }
        if(request.getStatus().equals(OwnerRequestStatus.REJECTED.toString())) {
            req.setStatus(OwnerRequestStatus.REJECTED);
            ownerRequestRepository.save(req);
            String description = "Yêu cầu làm chủ nhà của bạn đã bị từ chối.";
            emailService.sendRegisterSuccessEmail(user.getEmail(), user.getFullName(),description);
        }
        OwnerRequestResponse response = ownerRequestMapper.toResponse(req);
        return BaseResponse.<OwnerRequestResponse>builder()
                .data(response)
                .code(200)
                .message("Phê duyệt yêu cầu thành công")
                .build();
    }

    @Override
    @Transactional
    public PageResponse<OwnerRequestResponse> getRequests(OwnerRequestStatus status, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);

        Page<OwnerRequest> pageData;
        if (status != null) {
            pageData = ownerRequestRepository.findByStatus(status, pageable);
        } else {
            pageData = ownerRequestRepository.findAll(pageable);
        }

        Page<OwnerRequestResponse> mappedPage = pageData.map(ownerRequestMapper::toResponse);
        List<OwnerRequestResponse> responses = mappedPage.getContent().stream().toList();
        return PageResponse.<OwnerRequestResponse>builder()
                .pageSize(mappedPage.getSize())
                .pageNumber(mappedPage.getNumber())
                .totalElements(mappedPage.getTotalElements())
                .totalPages(mappedPage.getTotalPages())
                .data(responses)
                .message("Lấy danh sách yêu cầu chủ nhà")
                .code(200)
                .build();
    }
}