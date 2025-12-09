package com.example.room.service.Impl;

import com.example.room.dto.BaseResponse;
import com.example.room.dto.PageResponse;
import com.example.room.dto.request.RoomCreateRequest;
import com.example.room.dto.request.RoomUpdateRequest;
import com.example.room.dto.response.RoomResponse;
import com.example.room.elasticsearch.RoomSearchService;
import com.example.room.exception.ForBiddenException;
import com.example.room.exception.ResourceNotFoundException;
import com.example.room.mapper.RoomMapper;
import com.example.room.model.Room;
import com.example.room.model.User;
import com.example.room.repository.RoomRepository;
import com.example.room.repository.UserRepository;
import com.example.room.service.IStorageService;
import com.example.room.service.RoomService;
import com.example.room.specification.RoomSpecification;
import com.example.room.utils.Enums.RoleEnum;
import com.example.room.utils.Enums.RoomStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;
    private final RoomSearchService roomSearchService;
    private final IStorageService storageService;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BaseResponse<RoomResponse> createRoom(RoomCreateRequest request,List<MultipartFile> files) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!currentUser.getRole().getName().name().equals(RoleEnum.OWNER.name())) {
            throw new ForBiddenException("Chỉ chủ nhà mới có quyền tạo phòng trọ");
        }

        List<String> imageUrls = files.stream()
                .map(storageService::storeFile)
                .collect(Collectors.toList());

        Room room = Room.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .deposit(request.getDeposit())
                .area(request.getArea())
                .capacity(request.getCapacity())
                .address(request.getAddress())
                .status(RoomStatus.AVAILABLE)
                .owner(currentUser)
                .type(request.getType())
                .facilities(request.getFacilities())
                .images(imageUrls)
                .build();

        Room savedRoom = roomRepository.save(room);
        roomSearchService.indexRoom(savedRoom);
        return BaseResponse.<RoomResponse>builder()
                .code(201)
                .message("Tạo phòng thành công")
                .data(roomMapper.toResponse(savedRoom))
                .build();
    }

    @Override
    @Transactional
    public BaseResponse<RoomResponse> updateRoom(Long id, RoomUpdateRequest request,List<MultipartFile> files) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng với ID: " + id));

        if (request.getName() != null) room.setName(request.getName());
        if (request.getDescription() != null) room.setDescription(request.getDescription());
        if (request.getPrice() != null) room.setPrice(request.getPrice());
        if (request.getDeposit() != null) room.setDeposit(request.getDeposit());
        if (request.getArea() != null) room.setArea(request.getArea());
        if (request.getCapacity() != null) room.setCapacity(request.getCapacity());
        if (request.getAddress() != null) room.setAddress(request.getAddress());
        if (request.getStatus() != null) room.setStatus(request.getStatus());
        if (request.getType() != null) room.setType(request.getType());
        if (request.getFacilities() != null) room.setFacilities(request.getFacilities());
        if (files != null && !files.isEmpty()) {
            List<String> imageUrls = files.stream()
                    .map(storageService::storeFile)
                    .collect(Collectors.toList());

            room.setImages(imageUrls);
        }

        Room updatedRoom = roomRepository.save(room);
        roomSearchService.indexRoom(room);

        return BaseResponse.<RoomResponse>builder()
                .code(200)
                .message("Cập nhật phòng thành công")
                .data(roomMapper.toResponse(updatedRoom))
                .build();
    }
    
    @Override
    public BaseResponse<RoomResponse> getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng với ID: " + id));

        return BaseResponse.<RoomResponse>builder()
                .code(200)
                .message("Lây thông tin phòng thành công")
                .data(roomMapper.toResponse(room))
                .build();
    }

    @Override
    @Transactional
    public void deleteRoom(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng với ID: " + id));

        room.setDeleted(true); // Đánh dấu xóa mềm
        roomRepository.save(room);
        roomSearchService.indexRoom(room);
    }

    @Override
    public PageResponse<RoomResponse> searchRooms(String q, BigDecimal minPrice, BigDecimal maxPrice, Float minArea, int page, int size, String sort,String type,String status) {
        Sort sortOption = Sort.by(Sort.Direction.ASC, "createdAt");
        if ("desc".equalsIgnoreCase(sort)) {
            sortOption = Sort.by(Sort.Direction.DESC, "createdAt");
        }

        Pageable pageable = PageRequest.of(page, size, sortOption);
        Page<Room> roomPage = roomRepository.findAll(
                RoomSpecification.filterRooms(q, minPrice, maxPrice, minArea,type,status),
                pageable
        );

        List<RoomResponse> roomResponses = roomMapper.toResponse(roomPage.getContent());
        User user = null;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            user = null;
        } else {
            String email = auth.getName();
            user = userRepository.findByEmail(email).orElse(null);
        }
        if(user!= null && user.getRole().getName()==(RoleEnum.OWNER)){
            List<RoomResponse> filteredRooms = new ArrayList<>();
            for (RoomResponse roomResponse : roomResponses) {
                if(roomResponse.getOwnerId() == user.getId()){
                    filteredRooms.add(roomResponse);
                }
            }
            roomResponses = filteredRooms;
        }
        return PageResponse.<RoomResponse>builder()
                .code(200)
                .totalPages(roomPage.getTotalPages())
                .totalElements(roomPage.getTotalElements())
                .pageNumber(roomPage.getNumber())
                .pageSize(roomPage.getSize())
                .message("Lấy danh sách phòng thành công")
                .data(roomResponses)
                .build();
    }
}