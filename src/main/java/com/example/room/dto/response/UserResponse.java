package com.example.room.dto.response;

import com.example.room.utils.Enums.GenderEnum;
import com.example.room.utils.Enums.RoleEnum;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private String fullName;
    private String phone;
    @Enumerated(value = EnumType.STRING)
    private GenderEnum gender;
    private String address;
    private String citizenId;
    @Enumerated(value = EnumType.STRING)
    private RoleEnum roleName;
}
