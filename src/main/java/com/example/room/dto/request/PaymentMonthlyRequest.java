package com.example.room.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMonthlyRequest {

    private Long paymentId;
    private LocalDate paymentPeriod;

    private String roomName;
    private String roomAddress;
    private String ownerName;
    private String ownerPhone;
    private String ownerEmail;
    private BigDecimal baseRent;
    private List<ServiceItem> services;
    private BigDecimal servicesTotal;
    private BigDecimal grandTotal;
    private String vietQR;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceItem {
        private String name;
        private Integer quantityOld;
        private Integer quantityNew;
        private Integer quantityUsed;
        private BigDecimal pricePerUnit;
        private BigDecimal totalPrice;
    }
}
