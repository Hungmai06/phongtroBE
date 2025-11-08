package com.example.room.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankAccountRequest {

    @Size(max = 50, message = "Mã ngân hàng tối đa 50 ký tự")
    private String bankCode;

    @Size(max = 200, message = "Tên ngân hàng tối đa 200 ký tự")
    private String bankName;

    @NotBlank(message = "Số tài khoản không được để trống")
    @Size(max = 50, message = "Số tài khoản tối đa 50 ký tự")
    private String accountNumber;

    @Size(max = 200, message = "Tên chủ tài khoản tối đa 200 ký tự")
    private String accountName;

    private Long userId;
}
