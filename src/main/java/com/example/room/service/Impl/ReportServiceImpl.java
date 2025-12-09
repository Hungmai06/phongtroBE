package com.example.room.service.Impl;

import com.example.room.dto.response.RevenueGroupResponse;
import com.example.room.dto.response.RevenueSummaryResponse;
import com.example.room.exception.ForBiddenException;
import com.example.room.model.User;
import com.example.room.service.ReportService;
import com.example.room.service.SecurityService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    @PersistenceContext
    private EntityManager entityManager;

    private final SecurityService securityService;

    @Override
    public RevenueSummaryResponse getRevenueSummary(LocalDateTime start, LocalDateTime end, Long ownerId, Long roomId) {
        // Quyền truy cập:
        User currentUser = securityService.getCurrentUser();
        boolean isAdmin = currentUser.getRole() != null && currentUser.getRole().getName() != null &&
                currentUser.getRole().getName().name().equals("ADMIN");

        if (isAdmin) {
            // Admin chỉ được xem doanh thu của một chủ trọ (ownerId phải có), không được xem detail theo room
            if (ownerId == null) {
                throw new ForBiddenException("Admin phải cung cấp ownerId để xem báo cáo");
            }
            if (roomId != null) {
                throw new ForBiddenException("Admin không được xem báo cáo chi tiết theo phòng");
            }
        } else {
            // Owner: chỉ được xem báo cáo của chính mình. Nếu ownerId được truyền và khác current => forbidden
            Long currentOwnerId = currentUser.getId();
            if (ownerId != null && !ownerId.equals(currentOwnerId)) {
                throw new ForBiddenException("Owner không được xem báo cáo của chủ trọ khác");
            }
            ownerId = currentOwnerId; // nếu owner gọi mà ko truyền ownerId, gán mặc định

            // Nếu owner muốn xem theo phòng, kiểm tra quyền sở hữu phòng
            if (roomId != null) {
                if (!securityService.isRoomOwner(roomId)) {
                    throw new ForBiddenException("Owner không có quyền xem báo cáo của phòng này");
                }
            }
        }

        if (start == null) start = LocalDateTime.of(2025, 1, 1, 0, 0);
        if (end == null) end = LocalDateTime.now();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COALESCE(SUM(p.amount),0) AS total, COUNT(p.id) AS cnt ")
           .append("FROM payments p ")
           .append("JOIN bookings b ON p.booking_id = b.id ")
           .append("JOIN rooms r ON b.room_id = r.id ")
           .append("WHERE p.payment_date BETWEEN :start AND :end ")
                .append("AND p.payment_status = :paymentStatus ");

        if (ownerId != null) sql.append(" AND r.owner_id = :ownerId ");
        if (roomId != null) sql.append(" AND r.id = :roomId ");

        Query query = entityManager.createNativeQuery(sql.toString());
        query.setParameter("start", Timestamp.valueOf(start));
        query.setParameter("end", Timestamp.valueOf(end));
        if (ownerId != null) query.setParameter("ownerId", ownerId);
        if (roomId != null) query.setParameter("roomId", roomId);

        Object[] row = (Object[]) query.getSingleResult();
        BigDecimal total = BigDecimal.ZERO;
        long cnt = 0L;
        if (row != null) {
            Object totalObj = row[0];
            Object cntObj = row[1];
            if (totalObj != null) total = new BigDecimal(totalObj.toString());
            if (cntObj != null) cnt = ((Number) cntObj).longValue();
        }

        return new RevenueSummaryResponse(total, cnt);
    }

    @Override
    public List<RevenueGroupResponse> getMonthlyRevenue(LocalDateTime start, LocalDateTime end, Long ownerId, Long roomId) {
        // Quyền truy cập tương tự
        User currentUser = securityService.getCurrentUser();
        boolean isAdmin = currentUser.getRole() != null && currentUser.getRole().getName() != null &&
                currentUser.getRole().getName().name().equals("ADMIN");

        if (isAdmin) {
            if (ownerId == null) {
                throw new ForBiddenException("Admin phải cung cấp ownerId để xem báo cáo");
            }
            if (roomId != null) {
                throw new ForBiddenException("Admin không được xem báo cáo chi tiết theo phòng");
            }
        } else {
            Long currentOwnerId = currentUser.getId();
            if (ownerId != null && !ownerId.equals(currentOwnerId)) {
                throw new ForBiddenException("Owner không được xem báo cáo của chủ trọ khác");
            }
            ownerId = currentOwnerId;
            if (roomId != null && !securityService.isRoomOwner(roomId)) {
                throw new ForBiddenException("Owner không có quyền xem báo cáo của phòng này");
            }
        }

        if (start == null) start = LocalDateTime.of(2025, 1, 1, 0, 0);
        if (end == null) end = LocalDateTime.now();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT to_char(date_trunc('month', p.payment_date), 'YYYY-MM') AS period, ")
           .append("COALESCE(SUM(p.amount),0) AS total, COUNT(p.id) AS cnt ")
           .append("FROM payments p ")
           .append("JOIN bookings b ON p.booking_id = b.id ")
           .append("JOIN rooms r ON b.room_id = r.id ")
           .append("WHERE p.payment_date BETWEEN :start AND :end ")
                .append("AND p.payment_status = :paymentStatus ");;

        if (ownerId != null) sql.append(" AND r.owner_id = :ownerId ");
        if (roomId != null) sql.append(" AND r.id = :roomId ");

        sql.append(" GROUP BY date_trunc('month', p.payment_date) ")
           .append(" ORDER BY date_trunc('month', p.payment_date) DESC");

        Query query = entityManager.createNativeQuery(sql.toString());
        query.setParameter("start", Timestamp.valueOf(start));
        query.setParameter("end", Timestamp.valueOf(end));
        if (ownerId != null) query.setParameter("ownerId", ownerId);
        if (roomId != null) query.setParameter("roomId", roomId);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        List<RevenueGroupResponse> output = new ArrayList<>();
        for (Object[] r : results) {
            String period = r[0] != null ? r[0].toString() : null;
            BigDecimal total = r[1] != null ? new BigDecimal(r[1].toString()) : BigDecimal.ZERO;
            long cnt = r[2] != null ? ((Number) r[2]).longValue() : 0L;
            output.add(new RevenueGroupResponse(period, total, cnt));
        }

        return output;
    }
}
