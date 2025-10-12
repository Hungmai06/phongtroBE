package com.example.room.service;

import com.example.room.dto.BaseResponse;
import com.example.room.dto.PageResponse;
import com.example.room.dto.request.UserCreateRequest;
import com.example.room.dto.request.UserUpdateRequest;
import com.example.room.dto.response.UserResponse;
import jakarta.mail.MessagingException;

import java.time.LocalDateTime;

public interface UserService {
    BaseResponse<UserResponse> create(UserCreateRequest request);
    BaseResponse<UserResponse> update(Long id, UserUpdateRequest request);
    BaseResponse<UserResponse> findById(Long id);
    BaseResponse<String> delete(Long id);
    PageResponse<UserResponse>  search(String q, Integer page, Integer size,String sort);
    BaseResponse<UserResponse> updateRoleForOwner(Long id) throws MessagingException;
}