package com.example.room.service.Impl;

import com.example.room.dto.BaseResponse;
import com.example.room.dto.PageResponse;
import com.example.room.dto.request.RoleRequest;
import com.example.room.dto.response.RoleResponse;
import com.example.room.exception.InvalidDataException;
import com.example.room.exception.ResourceNotFoundException;
import com.example.room.mapper.RoleMapper;
import com.example.room.model.Role;
import com.example.room.repository.RoleRepository;
import com.example.room.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    @Override
    public BaseResponse<RoleResponse> create(RoleRequest request) {
        Optional<Role> optionalRole = roleRepository.findByName(request.getName());
        if(optionalRole.isPresent()){
            throw new InvalidDataException("Vai trò đã tồn tại");
        }
        Role role = Role.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        roleRepository.save(role);

        return BaseResponse.<RoleResponse>builder()
                .code(201)
                .data(roleMapper.toResponse(role))
                .message("Tạo vai trò thành công")
                .build();
    }

    @Override
    public BaseResponse<RoleResponse> update(Long id, RoleRequest request) {
        Role role = roleRepository.findById(id).orElseThrow(
                ()-> new ResourceNotFoundException("Vai trò không tồn tại")
        );

        role.setName(request.getName());
        role.setDescription(request.getDescription());
        roleRepository.save(role);
        return BaseResponse.<RoleResponse>builder()
                .code(200)
                .data(roleMapper.toResponse(role))
                .message("Cập nhật vai trò thành công")
                .build();
    }

    @Override
    public BaseResponse<RoleResponse> findById(Long id) {
        Role role = roleRepository.findById(id).orElseThrow(
                ()-> new ResourceNotFoundException("Vai trò không tồn tại")
        );
        return BaseResponse.<RoleResponse>builder()
                .code(200)
                .data(roleMapper.toResponse(role))
                .message("Lấy vai trò cho người dùng thành công")
                .build();
    }

    @Override
    public BaseResponse<String> delete(Long id) {
        Role role = roleRepository.findById(id).orElseThrow(
                ()-> new ResourceNotFoundException("Vai trò không tồn tại")
        );
        role.setDeleted(Boolean.TRUE);
        roleRepository.save(role);
        return BaseResponse.<String>builder()
                .code(204)
                .data("Đã xóa vai trò thành công")
                .message("Xóa vai trò")
                .build();
    }

    @Override
    public BaseResponse<List<RoleResponse>>getAll() {
        List<Role> roles = roleRepository.findAll();
        List<RoleResponse> roleResponses = roles.stream().map(roleMapper::toResponse).toList();
        return BaseResponse.<List<RoleResponse>>builder()
                .code(200)
                .message("Lấy danh sách vai trò ")
                .data(roleResponses)
                .build();
    }
}