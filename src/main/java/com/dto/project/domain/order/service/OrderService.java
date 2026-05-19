package com.dto.project.domain.order.service;

import com.dto.project.domain.member.entity.Member;
import com.dto.project.domain.order.entity.Order;
import com.dto.project.domain.order.repository.OrderRepository;
import com.dto.project.global.exception.DefaultErrorDetailMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;

    // 주문 목록 조회
    public List<Order> getOrderHistory(Member member) {
        return orderRepository.findByMemberOrderByCreatedAtDesc(member);
    }

    // 주문 상세 조회
    public Order getOrderDetail(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, DefaultErrorDetailMessages.NOT_FOUND));
    }

    // 주문 취소 로직
    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = getOrderDetail(orderId);
        order.cancel();
    }
}