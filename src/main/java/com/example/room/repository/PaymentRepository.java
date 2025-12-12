package com.example.room.repository;

import com.example.room.dto.response.OwnerRevenuePeriodDto;
import com.example.room.dto.response.RevenueGroupResponse;
import com.example.room.model.Payment;
import com.example.room.utils.Enums.PaymentStatus;
import com.example.room.utils.Enums.PaymentType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long>, JpaSpecificationExecutor<Payment> {
    boolean existsByBooking_IdAndPaymentTypeAndPaymentDateBetween(
            Long bookingId,
            com.example.room.utils.Enums.PaymentType paymentType,
            java.time.LocalDateTime startDate,
            java.time.LocalDateTime endDate
    );
    boolean existsByBooking_IdAndPaymentTypeAndDescription(
            Long bookingId,
            PaymentType paymentType,
            String description
    );
    Page<Payment> findByBooking_Room_Owner_Id(Long ownerId, Pageable pageable);
    Page<Payment> findByBooking_User_Id(Long renterId, Pageable pageable);

    @Query("""
    SELECT new com.example.room.dto.response.RevenueGroupResponse(
        p.paymentPeriod,
        SUM(p.amount)
    )
    FROM Payment p
    WHERE p.paymentStatus = com.example.room.utils.Enums.PaymentStatus.PAID
      AND (:ownerId IS NULL OR p.booking.room.owner.id = :ownerId)
      AND (:roomId IS NULL OR p.booking.room.id = :roomId)
      AND (:fromPeriod IS NULL OR p.paymentPeriod >= :fromPeriod)
      AND (:toPeriod IS NULL OR p.paymentPeriod <= :toPeriod)
    GROUP BY p.paymentPeriod
    ORDER BY p.paymentPeriod
    """)
    List<RevenueGroupResponse> getMonthlyRevenue(
            @Param("fromPeriod") String fromPeriod,
            @Param("toPeriod") String toPeriod,
            @Param("ownerId") Long ownerId,
            @Param("roomId") Long roomId
    );
    @Query("""
    SELECT COALESCE(SUM(p.amount), 0)
    FROM Payment p
    WHERE p.paymentStatus = com.example.room.utils.Enums.PaymentStatus.PAID
      AND (:ownerId IS NULL OR p.booking.room.owner.id = :ownerId)
      AND (:roomId IS NULL OR p.booking.room.id = :roomId)
      AND (:fromPeriod IS NULL OR p.paymentPeriod >= :fromPeriod)
      AND (:toPeriod IS NULL OR p.paymentPeriod <= :toPeriod)
    """)
    BigDecimal getTotalRevenueByPeriod(
            @Param("fromPeriod") String fromPeriod,   // "YYYY-MM"
            @Param("toPeriod")   String toPeriod,     // "YYYY-MM"
            @Param("ownerId")    Long ownerId,
            @Param("roomId")     Long roomId
    );


    @Query("""
    SELECT COALESCE(SUM(p.amount), 0)
    FROM Payment p
    JOIN p.booking b
    JOIN b.room r
    WHERE p.paymentStatus = :status
      AND r.owner.id = :ownerId
      AND p.paymentPeriod = :period
""")
    BigDecimal getOwnerRevenueInMonth(
            @Param("status") PaymentStatus status,
            @Param("ownerId") Long ownerId,
            @Param("period") String period
    );
}