package com.example.room.service;

import com.example.room.dto.BaseResponse;
import com.example.room.dto.request.RoomHasServiceCreateRequest;
import com.example.room.dto.response.RoomHasServiceResponse;

import java.util.List;

public interface RoomHasServiceService {

    BaseResponse<RoomHasServiceResponse> assignServiceToRoom(RoomHasServiceCreateRequest request);

    BaseResponse<List<RoomHasServiceResponse>> getServicesByRoom(Long roomId);

    BaseResponse<String> removeServiceFromRoom(Long id);
}