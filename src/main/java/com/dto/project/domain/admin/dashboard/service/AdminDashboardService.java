package com.dto.project.domain.admin.dashboard.service;

import com.dto.project.domain.admin.dashboard.dto.DashboardStatsResponse;
import com.dto.project.domain.member.entity.MemberStatus;
import com.dto.project.domain.member.repository.MemberRepository;
import com.dto.project.domain.order.entity.OrderStatus;
import com.dto.project.domain.order.repository.OrderRepository;
import com.dto.project.domain.qna.entity.QnaStatus;
import com.dto.project.domain.qna.repository.QnaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardService {

    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;
    private final QnaRepository qnaRepository;

    public DashboardStatsResponse getDashboardStats() {
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        return DashboardStatsResponse.builder()
                .totalMembers(memberRepository.countByStatus(MemberStatus.ACTIVE))
                .todayOrders(orderRepository.countByCreatedAtBetweenAndStatusNot(startOfDay, endOfDay, OrderStatus.CANCELLED))
                .pendingInquiries(qnaRepository.countByStatus(QnaStatus.WAITING))
                .totalRevenue(orderRepository.sumTotalAmountByStatusNot(OrderStatus.CANCELLED))
                .build();
    }
}