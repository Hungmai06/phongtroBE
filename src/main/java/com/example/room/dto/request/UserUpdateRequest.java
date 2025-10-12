package com.example.room.dto.request;

import com.example.room.utils.Enums.GenderEnum;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {
    private String email;

    private String fullName;

    private String phone;

    private String password;

    @Enumerated(value = EnumType.STRING)
    private GenderEnum gender;

    private String address;

    private String citizenId;

}
