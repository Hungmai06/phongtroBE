package com.example.room.service.Impl;

import com.example.room.dto.BaseResponse;
import com.example.room.dto.PageResponse;
import com.example.room.dto.request.RoomCreateRequest;
import com.example.room.dto.request.RoomUpdateRequest;
import com.example.room.dto.response.RoomResponse;
import com.example.room.exception.ForBiddenException;
import com.example.room.exception.ResourceNotFoundException;
import com.example.room.mapper.RoomMapper;
import com.example.room.model.Image;
import com.example.room.model.Room;
import com.example.room.model.User;
import com.example.room.repository.RoomRepository;
import com.example.room.repository.UserRepository;
import com.example.room.service.RoomService;
import com.example.room.specification.RoomSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.room.utils.Enums.RoleEnum;
import com.example.room.utils.Enums.RoomStatus;
import com.example.room.model.Image;
import com.example.room.service.IStorageService;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final RoomMapper roomMapper;
    private final IStorageService storageService;

    @Override
    @Transactional
    public RoomResponse createRoom(RoomCreateRequest request) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!currentUser.getRole().getName().name().equals(RoleEnum.OWNER.name())) {
            throw new ForBiddenException("Chỉ chủ nhà mới có quyền tạo phòng trọ");
        }

        Room room = Room.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .deposit(request.getDeposit())
                .area(request.getArea())
                .capacity(request.getCapacity())
                .address(request.getAddress())
                .utilities(request.getUtilities())
                .status(RoomStatus.AVAILABLE)
                .owner(currentUser)
                .build();
        if (request.getImages() != null && !request.getImages().isEmpty()) {
        List<Image> imageEntities = request.getImages().stream()
                .map(url -> Image.builder()
                        .imageUrl(url)
                        .room(room)
                        .build())
                .toList();
        room.setImages(imageEntities);
        }
        Room savedRoom = roomRepository.save(room);

        return roomMapper.toResponse(savedRoom);
    }

    @Override
    @Transactional
    public RoomResponse updateRoom(Long id, RoomUpdateRequest request) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng với ID: " + id));

        if (request.getName() != null) room.setName(request.getName());
        if (request.getDescription() != null) room.setDescription(request.getDescription());
        if (request.getPrice() != null) room.setPrice(request.getPrice());
        if (request.getDeposit() != null) room.setDeposit(request.getDeposit());
        if (request.getArea() != null) room.setArea(request.getArea());
        if (request.getCapacity() != null) room.setCapacity(request.getCapacity());
        if (request.getAddress() != null) room.setAddress(request.getAddress());
        if (request.getUtilities() != null) room.setUtilities(request.getUtilities());
        if (request.getStatus() != null) room.setStatus(request.getStatus());

        // Cập nhật danh sách ảnh: Xóa tất cả ảnh cũ và thêm danh sách ảnh mới
        if (request.getImages() != null) {
            // Khởi tạo danh sách nếu nó là null
            if (room.getImages() == null) {
                room.setImages(new ArrayList<>());
            }
            
            // Xóa các ảnh cũ
            room.getImages().clear();

            // Thêm các ảnh mới từ request
            List<Image> newImages = request.getImages().stream()
                    .map(imageUrl -> Image.builder().imageUrl(imageUrl).room(room).build())
                    .collect(Collectors.toList());
            room.getImages().addAll(newImages);
        }

        Room updatedRoom = roomRepository.save(room);
        
        // Trả về trực tiếp đối tượng đã được map
        return roomMapper.toResponse(updatedRoom);
    }
    
    @Override
    public RoomResponse getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng với ID: " + id));

        return roomMapper.toResponse(room);
    }

    @Override
    @Transactional
    public void deleteRoom(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng với ID: " + id));

        room.setDeleted(true); // Đánh dấu xóa mềm
        roomRepository.save(room);

    }

    @Override
    public PageResponse<RoomResponse> searchRooms(String q, BigDecimal minPrice, BigDecimal maxPrice, Float minArea, int page, int size, String sort) {
        Sort sortOption = Sort.by(Sort.Direction.ASC, "createdAt");
        if ("desc".equalsIgnoreCase(sort)) {
            sortOption = Sort.by(Sort.Direction.DESC, "createdAt");
        }

        Pageable pageable = PageRequest.of(page, size, sortOption);
        Page<Room> roomPage = roomRepository.findAll(
                RoomSpecification.filterRooms(q, minPrice, maxPrice, minArea),
                pageable
        );

        List<RoomResponse> roomResponses = roomPage.getContent().stream()
                .map(roomMapper::toResponse)
                .collect(Collectors.toList());

        return PageResponse.<RoomResponse>builder()
                .code(200)
                .message("Lấy danh sách phòng thành công")
                .data(roomResponses)
                .build();
    }
}