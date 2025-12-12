package com.example.room.service.Impl;

import com.example.room.dto.response.RevenueGroupResponse;
import com.example.room.dto.response.RevenueSummaryResponse;
import com.example.room.exception.ForBiddenException;
import com.example.room.model.User;
import com.example.room.repository.PaymentRepository;
import com.example.room.service.ReportService;
import com.example.room.service.SecurityService;
import com.sun.security.auth.UserPrincipal;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    @PersistenceContext
    private EntityManager entityManager;

    private final SecurityService securityService;
    private final PaymentRepository paymentRepository;

    @Override
    public List<RevenueGroupResponse> getMonthlyRevenue(
            String fromPeriod,
            String toPeriod,
            Long roomId
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();   // 👈 entity User

        boolean isOwner = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_OWNER"));
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Long effectiveOwnerId = null;

        if (isOwner) {
            effectiveOwnerId = user.getId(); // lấy từ quan hệ User → Owner
        }

        if (isAdmin) {
            effectiveOwnerId = null; // admin xem tất cả
        }

        if (toPeriod == null) {
            toPeriod = YearMonth.now().toString();
        }
        if (fromPeriod == null) {
            fromPeriod = YearMonth.parse(toPeriod).minusMonths(5).toString();
        }

        return paymentRepository.getMonthlyRevenue(fromPeriod, toPeriod, effectiveOwnerId, roomId);
    }
    @Override
    public RevenueSummaryResponse getRevenueSummaryByRecentMonths(Integer months,
                                                                  Long roomId) {

        // 1️⃣ Chuẩn hóa months: chỉ cho 3,6,9,12 – default = 6
        if (months == null) {
            months = 6;
        }
        if (months != 3 && months != 6 && months != 9 && months != 12) {
            months = 6;
        }

        // 2️⃣ Lấy user đang đăng nhập
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal(); // nếu bạn dùng entity User trong Security

        boolean isOwner = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_OWNER"));

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Long effectiveOwnerId = null;

        // OWNER → chỉ xem của mình
        if (isOwner) {
            // giả sử User có getOwner().getId()
            effectiveOwnerId = user.getId();
        }

        // ADMIN → xem full
        if (isAdmin) {
            effectiveOwnerId = null;
        }

        // 3️⃣ Tính fromPeriod & toPeriod theo months gần nhất
        YearMonth to = YearMonth.now();               // Tháng hiện tại, ví dụ 2025-12
        YearMonth from = to.minusMonths(months - 1);  // Ví dụ 6 tháng: 2025-07

        String fromPeriod = from.toString();          // "YYYY-MM"
        String toPeriod   = to.toString();            // "YYYY-MM"

        // 4️⃣ Gọi repo lấy tổng doanh thu
        BigDecimal total = paymentRepository.getTotalRevenueByPeriod(
                fromPeriod,
                toPeriod,
                effectiveOwnerId,
                roomId
        );

        // 5️⃣ Trả về DTO summary
        return new RevenueSummaryResponse(
                total != null ? total : BigDecimal.ZERO,
                fromPeriod,
                toPeriod,
                months
        );
    }
}
