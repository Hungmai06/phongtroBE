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

    @GetMapping("/revenue/summary/recent")
    @PreAuthorize("hasAnyRole('ADMIN','OWNER')")
    @Operation(summary = "Tổng doanh thu theo N tháng gần nhất (3/6/9/12)")
    public BaseResponse<RevenueSummaryResponse> getRevenueSummaryRecent(
            @RequestParam(required = false) Integer months,  // 3, 6, 9, 12
            @RequestParam(required = false) Long roomId      // nếu muốn lọc thêm theo phòng
    ) {
        RevenueSummaryResponse data =
                reportService.getRevenueSummaryByRecentMonths(months, roomId);

        return BaseResponse.<RevenueSummaryResponse>builder()
                .code(200)
                .message("OK")
                .data(data)
                .build();
    }

    @GetMapping("/revenue/monthly")
    @PreAuthorize("hasAnyRole('ADMIN','OWNER')")
    @Operation(summary = "Doanh thu theo tháng (group by paymentPeriod)")
    public BaseResponse<List<RevenueGroupResponse>> getMonthlyRevenue(
            @RequestParam(required = false) String fromPeriod,   // "YYYY-MM", ví dụ "2024-07"
            @RequestParam(required = false) String toPeriod,     // "YYYY-MM", ví dụ "2024-12"
            @RequestParam(required = false) Long roomId          // lọc thêm theo phòng nếu cần
    ) {
        /*
          - Không nhận ownerId từ FE.
          - Service sẽ:
              + Lấy user hiện tại từ SecurityContext
              + Nếu ROLE_OWNER  -> chỉ lấy dữ liệu của owner đó
              + Nếu ROLE_ADMIN  -> lấy toàn bộ (hoặc filter roomId nếu có)
              + Nếu from/to null -> tự tính 6 tháng gần nhất.
         */
        List<RevenueGroupResponse> data =
                reportService.getMonthlyRevenue(fromPeriod, toPeriod, roomId);

        return BaseResponse.<List<RevenueGroupResponse>>builder()
                .code(200)
                .message("OK")
                .data(data)
                .build();
    }
}
