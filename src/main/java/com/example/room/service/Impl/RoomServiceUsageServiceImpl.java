package com.example.room.service.Impl;

import com.example.room.dto.BaseResponse;
import com.example.room.dto.PageResponse;
import com.example.room.dto.request.RoomServiceUsageRequest;
import com.example.room.dto.response.RoomServiceUsageResponse;
import com.example.room.exception.ResourceNotFoundException;
import com.example.room.mapper.RoomServiceUsageMapper;
import com.example.room.model.Room;
import com.example.room.model.RoomHasService;
import com.example.room.model.RoomService;
import com.example.room.model.RoomServiceUsage;
import com.example.room.repository.RoomHasServiceRepository;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomServiceUsageServiceImpl implements RoomServiceUsageService {

    private final RoomServiceUsageRepository roomServiceUsageRepository;
    private final RoomRepository roomRepository;
    private final RoomServiceRepository roomServiceRepository;
    private final RoomServiceUsageMapper roomServiceUsageMapper;
    private final RoomHasServiceRepository roomHasServiceRepository;

    @Override
    public BaseResponse<RoomServiceUsageResponse> create(RoomServiceUsageRequest request) {
        if (!request.getMonth().matches("^\\d{4}-\\d{2}$")) {
            throw new IllegalArgumentException("Invalid month format. Expected format: YYYY-MM");
        }
            Room room = roomRepository.findById(request.getRoomId())
                    .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
            RoomService roomService = roomServiceRepository.findById(request.getRoomServiceId())
                    .orElseThrow(() -> new ResourceNotFoundException("RoomService not found"));

            RoomServiceUsage usage = RoomServiceUsage.builder()
                    .name(request.getName())
                    .quantityOld(request.getQuantityOld())
                    .quantityNew(request.getQuantityNew())
                    .month(request.getMonth())
                    .room(room)
                    .roomService(roomService)
                    .build();
            RoomHasService roomHasService = roomHasServiceRepository.findByRoomIdAndRoomServiceId(
                    request.getRoomId(), request.getRoomServiceId()
            ).orElseThrow(() -> new ResourceNotFoundException("Room does not have the specified service assigned"));

            calculateTotal(usage,roomHasService);
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
        if (!req.getMonth().matches("^\\d{4}-\\d{2}$")) {
            throw new IllegalArgumentException("Invalid month format. Expected format: YYYY-MM");
        }
            usage.setName(req.getName());
            usage.setQuantityOld(req.getQuantityOld());
            usage.setQuantityNew(req.getQuantityNew());
            usage.setMonth(req.getMonth());

        RoomHasService roomHasService = roomHasServiceRepository.findByRoomIdAndRoomServiceId(
                req.getRoomId(), req.getRoomServiceId()
        ).orElseThrow(() -> new ResourceNotFoundException("Room does not have the specified service assigned"));

             calculateTotal(usage,roomHasService);
            roomServiceUsageRepository.save(usage);
            RoomServiceUsageResponse resp = roomServiceUsageMapper.toResponse(usage);
            resp.setType(roomHasService.getType());
            return BaseResponse.<RoomServiceUsageResponse> builder()
                    .code(201)
                    .message("Room Service Usage Updated")
                    .data(resp)
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
    public BaseResponse<List<RoomServiceUsageResponse>> getByRoomIdAndMonth(Long roomId, String month) {
        List<RoomServiceUsage> usages = roomServiceUsageRepository.findByRoomIdAndMonth(roomId, month);
        List<RoomServiceUsageResponse> responses = usages.stream()
                .map(x -> {
                    RoomServiceUsageResponse res = roomServiceUsageMapper.toResponse(x);

                    roomHasServiceRepository
                            .findByRoomIdAndRoomServiceId(
                                    x.getRoom().getId(),
                                    x.getRoomService().getId()
                            )
                            .map(RoomHasService::getType)
                            .ifPresent(res::setType);

                    return res;
                })
                .toList();

        return BaseResponse.<List<RoomServiceUsageResponse>>builder()
                .data(responses)
                .message("Lấy danh sách sử dụng dịch vụ phòng theo tháng thành công")
                .code(200)
                .build();
    }

    @Override
    public BaseResponse<RoomServiceUsageResponse> getById(Long id) {

            RoomServiceUsage usage = roomServiceUsageRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Usage not found"));
            RoomServiceUsageResponse resp = roomServiceUsageMapper.toResponse(usage);
            roomHasServiceRepository
                    .findByRoomIdAndRoomServiceId(
                            usage.getRoom().getId(),
                            usage.getRoomService().getId()
                    )
                    .map(RoomHasService::getType)
                    .ifPresent(resp::setType);
            return BaseResponse.<RoomServiceUsageResponse> builder()
                    .code(201)
                    .message("Lấy dữu liệu thành công")
                    .data(resp)
                    .build();

    }

    @Override
    public PageResponse<RoomServiceUsageResponse> getAll(String month, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<RoomServiceUsage> result = null;
        if(month != null && !month.equals("")){
            result = roomServiceUsageRepository.findByMonth(month, pageable);
        }else{
            result = roomServiceUsageRepository.findAll(pageable);
        }
        List<RoomServiceUsageResponse> data = result.getContent().stream()
                .map(x -> {
                    RoomServiceUsageResponse res = roomServiceUsageMapper.toResponse(x);

                    roomHasServiceRepository
                            .findByRoomIdAndRoomServiceId(
                                    x.getRoom().getId(),
                                    x.getRoomService().getId()
                            )
                            .map(RoomHasService::getType)
                            .ifPresent(res::setType);

                    return res;
                })
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


    private void calculateTotal(RoomServiceUsage usage,RoomHasService roomHasService) {

        if (roomHasService.getType() == RoomServiceUsageStatus.METERED) {
            Integer used = (usage.getQuantityNew() != null && usage.getQuantityOld() != null)
                    ? usage.getQuantityNew() - usage.getQuantityOld()
                    : 0;
            usage.setQuantityUsed(used);
        } else {
            usage.setQuantityUsed(1);

        }

        BigDecimal total = roomHasService.getPricePerUnit()
                .multiply(BigDecimal.valueOf(usage.getQuantityUsed()));
        usage.setTotalPrice(total);
    }

}