package com.example.room.service;

import com.example.room.dto.response.RevenueGroupResponse;
import com.example.room.dto.response.RevenueSummaryResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface ReportService {

    List<RevenueGroupResponse> getMonthlyRevenue(String fromPeriod,
                                                 String toPeriod,
                                                 Long roomId);
    RevenueSummaryResponse getRevenueSummaryByRecentMonths(Integer months,
                                                           Long roomId);
}
