package com.example.room.controller;

import com.example.room.dto.BaseResponse;
import com.example.room.dto.response.RevenueGroupResponse;
import com.example.room.dto.response.RevenueSummaryResponse;
import com.example.room.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reports")
@Tag(name = "API REPORTS", description = "API cho báo cáo và thống kê")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/revenue/summary")
    @PreAuthorize("hasAnyRole('ADMIN','OWNER')")
    @Operation(summary = "Tổng quan doanh thu trong khoảng thời gian")
    public BaseResponse<RevenueSummaryResponse> getRevenueSummary(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(required = false) Long ownerId,
            @RequestParam(required = false) Long roomId
    ) {
        RevenueSummaryResponse data = reportService.getRevenueSummary(start, end, ownerId, roomId);
        return BaseResponse.<RevenueSummaryResponse>builder()
                .code(200)
                .data(data)
                .message("OK")
                .build();
    }

    @GetMapping("/revenue/monthly")
    @PreAuthorize("hasAnyRole('ADMIN','OWNER')")
    @Operation(summary = "Doanh thu theo tháng (group by month) trong khoảng thời gian")
    public BaseResponse<List<RevenueGroupResponse>> getMonthlyRevenue(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(required = false) Long ownerId,
            @RequestParam(required = false) Long roomId
    ) {
        List<RevenueGroupResponse> data = reportService.getMonthlyRevenue(start, end, ownerId, roomId);
        return BaseResponse.<List<RevenueGroupResponse>>builder()
                .code(200)
                .data(data)
                .message("OK")
                .build();
    }
}
