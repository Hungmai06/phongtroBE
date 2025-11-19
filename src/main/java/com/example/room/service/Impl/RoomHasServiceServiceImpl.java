package com.example.room.service.Impl;

import com.example.room.dto.BaseResponse;
import com.example.room.dto.request.RoomHasServiceCreateRequest;
import com.example.room.dto.response.RoomHasServiceResponse;
import com.example.room.exception.ResourceNotFoundException;
import com.example.room.mapper.RoomHasServiceMapper;
import com.example.room.model.Room;
import com.example.room.model.RoomHasService;
import com.example.room.model.RoomService;
import com.example.room.repository.RoomHasServiceRepository;
import com.example.room.repository.RoomRepository;
import com.example.room.repository.RoomServiceRepository;
import com.example.room.service.RoomHasServiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomHasServiceServiceImpl implements RoomHasServiceService {

    private final RoomRepository roomRepository;
    private final RoomServiceRepository roomServiceRepository;
    private final RoomHasServiceRepository roomHasServiceRepository;
    private final RoomHasServiceMapper mapper;

    @Override
    public BaseResponse<RoomHasServiceResponse> assignServiceToRoom(RoomHasServiceCreateRequest req) {

        Room room = roomRepository.findById(req.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Phòng không tồn tại"));

        RoomService service = roomServiceRepository.findById(req.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Dịch vụ không tồn tại"));

        RoomHasService relation = RoomHasService.builder()
                .room(room)
                .roomService(service)
                .build();

        RoomHasService saved = roomHasServiceRepository.save(relation);

        return BaseResponse.<RoomHasServiceResponse>builder()
                .code(201)
                .message("Gán dịch vụ cho phòng thành công")
                .data(mapper.toResponse(saved))
                .build();
    }

    @Override
    public BaseResponse<List<RoomHasServiceResponse>> getServicesByRoom(Long roomId) {

        List<RoomHasService> list = roomHasServiceRepository.findByRoomId(roomId);

        List<RoomHasServiceResponse> responses = list.stream()
                .map(mapper::toResponse)
                .toList();

        return BaseResponse.<List<RoomHasServiceResponse>>builder()
                .code(200)
                .message("Danh sách dịch vụ theo phòng")
                .data(responses)
                .build();
    }

    @Override
    public BaseResponse<String> removeServiceFromRoom(Long id) {

        RoomHasService entity = roomHasServiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dịch vụ này trong phòng"));

        entity.setDeleted(Boolean.TRUE);
        roomHasServiceRepository.save(entity);
        return BaseResponse.<String>builder()
                .code(200)
                .message("Xóa dịch vụ khỏi phòng thành công")
                .data("Deleted")
                .build();
    }
}