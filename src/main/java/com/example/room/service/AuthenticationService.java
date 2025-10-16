package com.example.room.service;

import com.example.room.dto.request.RefreshTokenRequest;
import com.example.room.dto.request.SignInRequest;
import com.example.room.dto.response.TokenResponse;

public interface AuthenticationService {
    TokenResponse getAccessToken(SignInRequest request);
    TokenResponse getRefreshToken(RefreshTokenRequest request);
}