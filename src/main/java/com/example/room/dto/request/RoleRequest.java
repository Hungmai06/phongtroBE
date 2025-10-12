package com.example.room.dto.request;

import com.example.room.utils.Enums.RoleEnum;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class RoleRequest {
    @NotNull(message = "Tên vai trò không được để trống")
    @Enumerated(EnumType.STRING)
    private RoleEnum name;

    @NotBlank(message = "Mô tả không được để trống")
    private String description;
}
