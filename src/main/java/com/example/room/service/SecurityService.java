package com.example.room.service;

import com.example.room.exception.ResourceNotFoundException;
import com.example.room.model.*;
import com.example.room.repository.*;
import com.example.room.utils.Enums.RoleEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service("securityService")
@RequiredArgsConstructor
public class SecurityService {

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final ContractRepository contractRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;

    /**
     * ✅ Lấy thông tin người dùng hiện tại từ SecurityContextHolder
     */
    public User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User user) {
            return user;
        }
        throw new ResourceNotFoundException("User not authenticated or invalid principal");
    }

    /**
     * ✅ Kiểm tra xem user hiện tại có phải là chủ sở hữu (owner) của room không
     */
    public boolean isRoomOwner(Long roomId) {
        User currentUser = getCurrentUser();
        if (isAdmin(currentUser)) return true;

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng ID: " + roomId));

        return room.getOwner().getId().equals(currentUser.getId());
    }

    /**
     * ✅ Kiểm tra quyền truy cập Booking:
     * - Owner có quyền nếu phòng trong booking là của mình
     * - Renter có quyền nếu booking là của chính họ
     */
    public boolean canAccessBooking(Long bookingId) {
        User currentUser = getCurrentUser();
        if (isAdmin(currentUser)) return true;

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy booking ID: " + bookingId));

        boolean isOwner = booking.getRoom().getOwner().getId().equals(currentUser.getId());
        boolean isRenter = booking.getUser().getId().equals(currentUser.getId());

        return isOwner || isRenter;
    }

    /**
     * ✅ Kiểm tra quyền truy cập hợp đồng
     */
    public boolean canAccessContract(Long contractId) {
        User currentUser = getCurrentUser();
        if (isAdmin(currentUser)) return true;

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hợp đồng ID: " + contractId));

        Long ownerId = contract.getBooking().getRoom().getOwner().getId();
        Long renterId = contract.getBooking().getUser().getId();

        return ownerId.equals(currentUser.getId()) || renterId.equals(currentUser.getId());
    }

    /**
     * ✅ Kiểm tra quyền truy cập hóa đơn
     */
    public boolean canAccessInvoice(Long invoiceId) {
        User currentUser = getCurrentUser();
        if (isAdmin(currentUser)) return true;

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn ID: " + invoiceId));

        Long ownerId = invoice.getContract().getBooking().getRoom().getOwner().getId();
        Long renterId = invoice.getUser().getId();

        return ownerId.equals(currentUser.getId()) || renterId.equals(currentUser.getId());
    }

    /**
     * ✅ Kiểm tra quyền truy cập thanh toán
     */
    public boolean canAccessPayment(Long paymentId) {
        User currentUser = getCurrentUser();
        if (isAdmin(currentUser)) return true;

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy payment ID: " + paymentId));

        Long ownerId = payment.getBooking().getRoom().getOwner().getId();
        Long renterId = payment.getBooking().getUser().getId();

        return ownerId.equals(currentUser.getId()) || renterId.equals(currentUser.getId());
    }

    /**
     * ✅ Kiểm tra quyền ADMIN
     */
    private boolean isAdmin(User user) {
        return user.getRole().getName().name().equals(RoleEnum.ADMIN.name());
    }
}
