package com.example.room.repository;

import com.example.room.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Page<Invoice> findByUser_Id(Long userId, Pageable pageable);
    Page<Invoice> findByContract_Booking_Room_Owner_Id(Long ownerId, Pageable pageable);
    boolean existsByPaymentId(Long paymentId);
}