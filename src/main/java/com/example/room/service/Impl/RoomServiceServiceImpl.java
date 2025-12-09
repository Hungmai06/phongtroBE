package com.example.room.service.Impl;

import com.example.room.dto.BaseResponse;
import com.example.room.dto.PageResponse;
import com.example.room.dto.request.RoomServiceCreateRequest;
import com.example.room.dto.response.RoomServiceResponse;
import com.example.room.exception.ResourceNotFoundException;
import com.example.room.mapper.RoomServiceMapper;
import com.example.room.model.Room;
import com.example.room.model.RoomService;
import com.example.room.repository.RoomRepository;
import com.example.room.repository.RoomServiceRepository;
import com.example.room.service.RoomServiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class RoomServiceServiceImpl implements RoomServiceService {

    private final RoomServiceRepository roomServiceRepository;
    private final RoomRepository roomRepository;
    private final RoomServiceMapper roomServiceMapper;

    @Override
    public BaseResponse<RoomServiceResponse> create(RoomServiceCreateRequest request) {
        RoomService roomService = RoomService.builder()
                .description(request.getDescription())
                .name(request.getName())
                .build();
        roomServiceRepository.save(roomService);
        return BaseResponse.<RoomServiceResponse> builder()
                .code(201)
                .data(roomServiceMapper.toResponse(roomService))
                .message("Room created successfully")
                .build();
    }

    @Override
    public BaseResponse<RoomServiceResponse> update(Long id, RoomServiceCreateRequest request) {
        RoomService existing = roomServiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dịch vụ phòng không tồn tại"));
        // copy mutable fields
        existing.setName(request.getName());
        existing.setDescription(request.getDescription());

        RoomService saved = roomServiceRepository.save(existing);
        return BaseResponse.<RoomServiceResponse> builder()
                .code(200)
                .data(roomServiceMapper.toResponse(saved))
                .message("Room updated successfully")
                .build();
    }

    @Override
    public void delete(Long id) {
        Optional<RoomService> optional = roomServiceRepository.findById(id);
        if (optional.isEmpty()) {
            throw new ResourceNotFoundException("Dịch vụ phòng không tồn tại");
        }
        RoomService roomService = optional.get();
        roomService.setDeleted(Boolean.TRUE);
        roomServiceRepository.save(roomService);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<RoomServiceResponse> findById(Long id) {
        RoomService entity = roomServiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dịch vụ phòng không tồn tại"));
        return BaseResponse.<RoomServiceResponse> builder()
                .code(200)
                .data(roomServiceMapper.toResponse(entity))
                .message("Get room by id service successfully")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RoomServiceResponse> findAll(Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<RoomService> roomServicePage = roomServiceRepository.findAll(pageable);

        List<RoomServiceResponse> content = roomServicePage.getContent().stream()
                .map(roomServiceMapper::toResponse) // dùng mapper thay vì this::toResponse
                .collect(Collectors.toList());

        return PageResponse.<RoomServiceResponse>builder()
                .data(content)
                .code(200)
                .message("Get all room service successfully")
                .pageNumber(roomServicePage.getNumber())
                .pageSize(roomServicePage.getSize())
                .totalElements(roomServicePage.getTotalElements())
                .totalPages(roomServicePage.getTotalPages())
                .build();
    }
}
