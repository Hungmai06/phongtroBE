package com.example.room.service;

import com.example.room.dto.BaseResponse;
import com.example.room.dto.PageResponse;
import com.example.room.dto.request.OwnerRequestRequest;
import com.example.room.dto.request.OwnerRequestUpdate;
import com.example.room.dto.response.OwnerRequestResponse;
import com.example.room.utils.Enums.OwnerRequestStatus;
import jakarta.mail.MessagingException;

public interface OwnerRequestService {

    BaseResponse<OwnerRequestResponse> createRequest( OwnerRequestRequest dto);

    BaseResponse<OwnerRequestResponse> handle(OwnerRequestUpdate requestUpdate) throws MessagingException;

    PageResponse<OwnerRequestResponse> getRequests(OwnerRequestStatus status, int page, int size);
}
