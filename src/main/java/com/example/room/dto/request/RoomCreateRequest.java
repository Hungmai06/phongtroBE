package com.example.room.dto.request;

import com.example.room.utils.Enums.RoomType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import jakarta.validation.constraints.NotEmpty;

@Getter
@Setter
public class RoomCreateRequest {

    @NotBlank(message = "Tên phòng không được để trống")
    private String name;

    @NotBlank(message = "Mô tả không được để trống")
    private String description;

    @NotNull(message = "Giá phòng không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá phòng phải lớn hơn 0")
    private BigDecimal price;

    @NotNull(message = "Tiền cọc không được để trống")
    @DecimalMin(value = "0.0", message = "Tiền cọc không được nhỏ hơn 0")
    private BigDecimal deposit;

    @NotNull(message = "Diện tích không được để trống")
    private Float area;

    private Integer capacity;

    @NotNull(message = "Loại phòng không được để trống")
    private RoomType type;

    @NotBlank(message = "Địa chỉ không được để trống")
    private String address;

    private String utilities;

}