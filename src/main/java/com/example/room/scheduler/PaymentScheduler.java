package com.example.room.scheduler;

import com.example.room.service.Impl.PaymentServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class PaymentScheduler {

    private final PaymentServiceImpl paymentService;

    /**
     * Lên lịch chạy vào 9h sáng ngày 1 hàng tháng.
     * Cron: giây phút giờ ngày tháng thứ
     */
    @Scheduled(cron = "0 0 9 1 * ?")
    public void runMonthlyPaymentGeneration() {
        log.info("🕐 Bắt đầu tiến trình tạo payment hàng tháng...");
        paymentService.generateMonthlyPayments();
        log.info("✅ Hoàn thành tạo payment hàng tháng!");
    }
}
