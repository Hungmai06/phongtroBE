package com.example.room.dto.request;

import com.example.room.utils.Enums.OwnerRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OwnerRequestUpdate {
    private Long requestId;
    private String status;
}
