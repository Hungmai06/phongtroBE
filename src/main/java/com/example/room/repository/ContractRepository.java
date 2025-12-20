package com.example.room.repository;

import com.example.room.model.Contract;
import com.example.room.utils.Enums.ContractStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {
    List<Contract> findByStatus(ContractStatus status);
    List<Contract> findByBookingIdAndStatus(Long bookingId, ContractStatus status);
    Page<Contract> findByBooking_User_Id(Long userId, Pageable pageable);
    Page<Contract> findByBooking_Room_Owner_Id(Long ownerId, Pageable pageable);
    Optional<Contract> findByBookingId(Long bookingId);

    @Query("select c.id from Contract c where c.booking.room.owner.id = :ownerId")
    List<Long> findIdsByBooking_Room_Owner_Id(@Param("ownerId") Long ownerId);
    @Query("""
    SELECT c FROM Contract c
    WHERE c.booking.room.id = :roomId
      AND c.status = :status
""")
    Optional<Contract> findActiveContractByRoomId(@Param("roomId") Long roomId,
                                                  @Param("status") ContractStatus status);
    @Query("""
    SELECT c FROM Contract c
    WHERE c.booking.room.owner.id = :ownerId
      AND c.status = :status
      AND (c.startDate IS NULL OR c.startDate <= :now)
      AND (c.endDate IS NULL OR c.endDate >= :now)
""")
    List<Contract> findActiveContractsByOwner(@Param("ownerId") Long ownerId,
                                              @Param("status") ContractStatus status,
                                              @Param("now") LocalDateTime now);
}