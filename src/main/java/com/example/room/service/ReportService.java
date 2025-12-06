package com.example.room.service;

import com.example.room.dto.response.RevenueGroupResponse;
import com.example.room.dto.response.RevenueSummaryResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface ReportService {

    /**
     * Trả về tổng doanh thu và số lượng khoản thanh toán trong khoảng thời gian.
     * Nếu start hoặc end null thì sẽ dùng khoảng mặc định (start = 1970-01-01, end = now).
     */
    RevenueSummaryResponse getRevenueSummary(LocalDateTime start, LocalDateTime end, Long ownerId, Long roomId);

    /**
     * Trả về doanh thu nhóm theo tháng trong khoảng thời gian (period format YYYY-MM), sắp xếp giảm dần.
     */
    List<RevenueGroupResponse> getMonthlyRevenue(LocalDateTime start, LocalDateTime end, Long ownerId, Long roomId);
}
