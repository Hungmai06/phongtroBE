package com.example.room.controller;

import com.example.room.dto.BaseResponse;
import com.example.room.dto.request.RoleRequest;
import com.example.room.dto.response.RoleResponse;
import com.example.room.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/role")
@Tag(name = "API ROLE",description = "API cho vai trò")
public class RoleController {
    private final RoleService roleService;
    @PostMapping("")
    @Operation(summary = "Tạo vai trò")
    public BaseResponse<RoleResponse> create(@Valid @RequestBody RoleRequest request){
        return  roleService.create(request);
    }

    @GetMapping("")
    @Operation(summary = "Lấy danh sách vai trò")
    public BaseResponse<List<RoleResponse>> getAll(){
        return  roleService.getAll();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật vai trò")
    public BaseResponse<RoleResponse> update(@PathVariable Long id, @Valid @RequestBody RoleRequest request){
        return  roleService.update(id, request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy vai trò theo id")
    public BaseResponse<RoleResponse> getRoleById(@PathVariable Long id){
        return  roleService.findById(id);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa vai trò")
    public BaseResponse<String> delete(@PathVariable Long id){
        return  roleService.delete(id);
    }

}
