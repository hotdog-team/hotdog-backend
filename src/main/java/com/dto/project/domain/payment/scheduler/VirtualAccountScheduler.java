package com.dto.project.domain.payment.scheduler;

import com.dto.project.domain.order.entity.Order;
import com.dto.project.domain.order.entity.OrderStatus;
import com.dto.project.domain.order.entity.PaymentMethod;
import com.dto.project.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class VirtualAccountScheduler {

    private final OrderRepository orderRepository;

    // 매일 자정에 실행하여 3일 이상 지난 입금 대기건 취소
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void cancelExpiredVirtualAccounts() {
        log.info("=== 무통장 입금 미결제 3일 경과 취소 스케줄러 실행 ===");

        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);

        List<Order> expiredOrders = orderRepository.findExpiredPendingOrders(threeDaysAgo, OrderStatus.PENDING, PaymentMethod.CASH);

        for (Order order : expiredOrders) {
            order.updateStatus("CANCELED");
            log.info("자동 취소 완료 - 주문 ID: {}", order.getId());
        }
    }
}