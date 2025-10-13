package com.example.room.controller;

import com.example.room.dto.request.RefreshTokenRequest;
import com.example.room.dto.request.SignInRequest;
import com.example.room.dto.response.TokenResponse;
import com.example.room.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication Controller")
public class AuthenticationController {
    private final AuthenticationService authenticationService;
//    private final RedisOtpService redisOtpService;
    @PostMapping("/login")
    @Operation(summary = "Access Token")
    public TokenResponse getAccessToken(@RequestBody SignInRequest request) {
        return authenticationService.getAccessToken(request);
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh Token")
    public TokenResponse getRefreshToken(@RequestBody RefreshTokenRequest request){
        return authenticationService.getRefreshToken(request);
    }

//    @PostMapping("/forgot-password")
//    @Operation(summary = "Send Otp")
//    public void sendOtp(@RequestBody ForgotPasswordRequest request) throws MessagingException {
//        redisOtpService.handleSendOtpByUsername(request.getUsername());
//    }
//
//    @PostMapping("/reset-password")
//    @Operation(summary = "Reset Password")
//    public ApiResponse<String> resetPassword(@RequestBody ResetPasswordRequest request) {
//        return redisOtpService.handleResetPassword(request);
//    }
}