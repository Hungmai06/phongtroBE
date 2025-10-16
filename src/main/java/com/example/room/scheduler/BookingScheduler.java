package com.example.room.scheduler;

import com.example.room.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingScheduler {

    private final BookingService bookingService;

    /**
     * Lên lịch chạy 5 phút một lần để kiểm tra và hủy các booking quá hạn.
     * Cron: giây phút giờ ngày tháng thứ
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void runCancelExpiredBookings() {
        log.info("⏰ Bắt đầu tiến trình quét và hủy booking quá hạn...");
        bookingService.cancelExpiredBookings();
        log.info("✅ Hoàn thành tiến trình hủy booking quá hạn.");
    }
}