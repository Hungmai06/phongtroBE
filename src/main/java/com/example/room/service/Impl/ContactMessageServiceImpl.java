package com.example.room.service.Impl;

import com.example.room.dto.BaseResponse;
import com.example.room.dto.PageResponse;
import com.example.room.dto.request.ContactMessageRequest;
import com.example.room.dto.response.ContactMessageResponse;
import com.example.room.exception.ResourceNotFoundException;
import com.example.room.model.Help;
import com.example.room.repository.HelpRepository;
import com.example.room.service.ContactMessageService;
import com.example.room.service.EmailService;
import com.example.room.specification.ContactMessageSpecification;
import com.example.room.utils.Enums.ContactStatus;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContactMessageServiceImpl implements ContactMessageService {

    private final HelpRepository contactMessageRepository;
    private final EmailService emailService;
    private ContactMessageResponse toResponse(Help entity) {
        if (entity == null) return null;

        return ContactMessageResponse.builder()
                .id(entity.getId())
                .fullName(entity.getFullName())
                .phone(entity.getPhone())
                .email(entity.getEmail())
                .subject(entity.getSubject())
                .message(entity.getMessage())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }


    private PageResponse<ContactMessageResponse> toPageResponse(Page<Help> page) {
        return PageResponse.<ContactMessageResponse>builder()
                .code(200)
                .message("Lấy danh sách liên hệ thành công")
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .data(page.getContent().stream().map(this::toResponse).toList())
                .build();
    }


    @Override
    public BaseResponse<ContactMessageResponse> createContact(ContactMessageRequest request) {

        Help entity = Help.builder()
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .subject(request.getSubject())
                .message(request.getMessage())
                .status(ContactStatus.NEW)
                .build();

        Help saved = contactMessageRepository.save(entity);

        return BaseResponse.<ContactMessageResponse>builder()
                .code(200)
                .message("Gửi liên hệ thành công")
                .data(toResponse(saved))
                .build();
    }

    @Override
    public BaseResponse<ContactMessageResponse> getContactById(Long id) {
        Help entity = contactMessageRepository.findById(id).orElseThrow(
                ()-> new ResourceNotFoundException("Liên hệ không tồn tại")
        );

        return BaseResponse.<ContactMessageResponse>builder()
                .code(200)
                .message("Lấy chi tiết liên hệ thành công")
                .data(toResponse(entity))
                .build();
    }

    @Override
    public PageResponse<ContactMessageResponse> searchContacts(String q, int page, int size) {

        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Help> spec = ContactMessageSpecification.filter(q);

        Page<Help> contactPage = contactMessageRepository.findAll(spec, pageable);

        return toPageResponse(contactPage);
    }
    @Override
    public BaseResponse<ContactMessageResponse> updateStatus(Long id, ContactMessageRequest request) throws MessagingException {
        Help entity = contactMessageRepository.findById(id).orElseThrow(
                ()-> new ResourceNotFoundException("Liên hệ không tồn tại")
        );
        if(request.getStatus().equals(ContactStatus.APPROVED)){
            emailService.sendHelp(entity.getFullName(), entity.getEmail(), "Yêu cầu hỗ trợ của bạn đã được chấp nhận. Chúng tôi đang hỗ trợ bạn giải quyết vấn đề");
        }else{
            emailService.sendHelp(entity.getFullName(), entity.getEmail(), "Yêu cầu hỗ trợ của bạn đã bị từ chối. Vui lòng liên hệ lại nếu cần hỗ trợ thêm.");
        }
        entity.setStatus(request.getStatus());
        Help saved = contactMessageRepository.save(entity);

        return BaseResponse.<ContactMessageResponse>builder()
                .code(200)
                .message("Cập nhật trạng thái liên hệ thành công")
                .data(toResponse(saved))
                .build();
    }
}