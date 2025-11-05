package com.example.room.service.Impl;

import com.example.room.dto.BaseResponse;
import com.example.room.dto.PageResponse;
import com.example.room.dto.request.RoomServiceUsageRequest;
import com.example.room.dto.response.RoomServiceUsageResponse;
import com.example.room.exception.ResourceNotFoundException;
import com.example.room.mapper.RoomServiceUsageMapper;
import com.example.room.model.Room;
import com.example.room.model.RoomService;
import com.example.room.model.RoomServiceUsage;
import com.example.room.repository.RoomRepository;
import com.example.room.repository.RoomServiceRepository;
import com.example.room.repository.RoomServiceUsageRepository;
import com.example.room.service.RoomServiceUsageService;
import com.example.room.utils.Enums.RoomServiceUsageStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomServiceUsageServiceImpl implements RoomServiceUsageService {

    private final RoomServiceUsageRepository roomServiceUsageRepository;
    private final RoomRepository roomRepository;
    private final RoomServiceRepository roomServiceRepository;
    private final RoomServiceUsageMapper roomServiceUsageMapper;

    @Override
    public BaseResponse<RoomServiceUsageResponse> create(RoomServiceUsageRequest request) {

            Room room = roomRepository.findById(request.getRoomId())
                    .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
            RoomService roomService = roomServiceRepository.findById(request.getRoomServiceId())
                    .orElseThrow(() -> new ResourceNotFoundException("RoomService not found"));

            RoomServiceUsage usage = RoomServiceUsage.builder()
                    .name(request.getName())
                    .type(request.getType())
                    .quantityOld(request.getQuantityOld())
                    .quantityNew(request.getQuantityNew())
                    .pricePerUnit(request.getPricePerUnit())
                    .month(request.getMonth())
                    .usedAt(request.getUsedAt())
                    .room(room)
                    .roomService(roomService)
                    .build();


            calculateTotal(usage);
            roomServiceUsageRepository.save(usage);

            return BaseResponse.<RoomServiceUsageResponse> builder()
                    .code(201)
                    .message("Room Service Usage Created")
                    .data(roomServiceUsageMapper.toResponse(usage))
                    .build();
    }

    @Override
    public BaseResponse<RoomServiceUsageResponse> update(Long id, RoomServiceUsageRequest req) {
            RoomServiceUsage usage = roomServiceUsageRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Usage not found"));

            usage.setName(req.getName());
            usage.setType(req.getType());
            usage.setQuantityOld(req.getQuantityOld());
            usage.setQuantityNew(req.getQuantityNew());
            usage.setPricePerUnit(req.getPricePerUnit());
            usage.setMonth(req.getMonth());
            usage.setUsedAt(req.getUsedAt());

            calculateTotal(usage);
            roomServiceUsageRepository.save(usage);

            return BaseResponse.<RoomServiceUsageResponse> builder()
                    .code(201)
                    .message("Room Service Usage Updated")
                    .data(roomServiceUsageMapper.toResponse(usage))
                    .build();
    }

    @Override
    public void delete(Long id) {
        RoomServiceUsage usage = roomServiceUsageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usage not found"));
        usage.setDeleted(Boolean.TRUE);
        roomServiceUsageRepository.save(usage);
    }

    @Override
    public BaseResponse<RoomServiceUsageResponse> getById(Long id) {

            RoomServiceUsage usage = roomServiceUsageRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Usage not found"));
            return BaseResponse.<RoomServiceUsageResponse> builder()
                    .code(201)
                    .message("Lấy dữu liệu thành công")
                    .data(roomServiceUsageMapper.toResponse(usage))
                    .build();

    }

    @Override
    public PageResponse<RoomServiceUsageResponse> getAll(LocalDateTime month, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<RoomServiceUsage> result = (month != null)
                ? roomServiceUsageRepository.findByMonth(month, pageable)
                : roomServiceUsageRepository.findAll(pageable);
        List<RoomServiceUsageResponse> data = result.getContent().stream()
                .map(roomServiceUsageMapper::toResponse)
                .toList();
        return PageResponse.<RoomServiceUsageResponse>builder()
                .totalPages(result.getTotalPages())
                .totalElements(result.getTotalElements())
                .pageNumber(result.getNumber())
                .pageSize(result.getSize())
                .data(data)
                .code(200)
                .message("Lấy danh sách sử dụng dịch vụ phòng thành công")
                .build();
    }


    private void calculateTotal(RoomServiceUsage usage) {

        if (usage.getType() == RoomServiceUsageStatus.METERED) {
            Integer used = (usage.getQuantityNew() != null && usage.getQuantityOld() != null)
                    ? usage.getQuantityNew() - usage.getQuantityOld()
                    : 0;
            usage.setQuantityUsed(used);
        } else {
            usage.setQuantityUsed(1);
        }

        BigDecimal total = usage.getPricePerUnit()
                .multiply(BigDecimal.valueOf(usage.getQuantityUsed()));
        usage.setTotalPrice(total);
    }

}