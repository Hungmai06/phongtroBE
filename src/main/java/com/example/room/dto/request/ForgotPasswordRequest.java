package com.example.room.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordRequest {
    @NotNull(message = "Email không được để trống")
    private String email;
    @NotNull(message = "OTP không được để trống")
    private String otp;
    @NotNull(message = "Mật khẩu mới không được để trống")
    private String newPassword;
}
