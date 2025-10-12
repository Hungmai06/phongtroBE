package com.example.room.controller;

import com.example.room.dto.BaseResponse;
import com.example.room.dto.PageResponse;
import com.example.room.dto.request.RoleRequest;
import com.example.room.dto.request.UserCreateRequest;
import com.example.room.dto.request.UserUpdateRequest;
import com.example.room.dto.response.RoleResponse;
import com.example.room.dto.response.UserResponse;
import com.example.room.service.RoleService;
import com.example.room.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
@Tag(name = "API USER",description = "API cho người dùng")
public class UserController {
    private final UserService userService;

    @PostMapping("")
    @Operation(summary = "Tạo Người dùng")
    public BaseResponse<UserResponse> create(@Valid @RequestBody UserCreateRequest request){
        return  userService.create(request);
    }

    @GetMapping("")
    @Operation(summary = "Lấy danh sách người dùng")
    public PageResponse<UserResponse> getAll(@RequestParam(required = false) String q,@RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size,@RequestParam(required = false) String sort){
        return  userService.search(q,page,size,sort);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật thông tin người dùng")
    public BaseResponse<UserResponse> update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request){
        return  userService.update(id, request);
    }

    @PutMapping("/{id}/role")
    @Operation(summary = "Cập nhật quyền cho người dùng")
    public BaseResponse<UserResponse> updateRole(@PathVariable Long id) throws MessagingException {
        return  userService.updateRoleForOwner(id);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy thông tin người dùng theo id")
    public BaseResponse<UserResponse> getUserById(@PathVariable Long id){
        return  userService.findById(id);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa người dùng")
    public BaseResponse<String> delete(@PathVariable Long id){
        return  userService.delete(id);
    }

}
