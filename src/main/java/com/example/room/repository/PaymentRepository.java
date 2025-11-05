package com.example.room.repository;

import com.example.room.model.Payment;
import com.example.room.utils.Enums.PaymentType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
}