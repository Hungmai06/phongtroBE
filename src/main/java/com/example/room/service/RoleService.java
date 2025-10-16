package com.example.room.service;

import com.example.room.dto.BaseResponse;
import com.example.room.dto.PageResponse;
import com.example.room.dto.request.RoleRequest;
import com.example.room.dto.response.RoleResponse;

import java.util.List;

public interface RoleService {
 BaseResponse<RoleResponse> create(RoleRequest request);
 BaseResponse<RoleResponse> update(Long id, RoleRequest request);
 BaseResponse<RoleResponse> findById(Long id);
 BaseResponse<String> delete(Long id);
 BaseResponse<List<RoleResponse>> getAll();
}