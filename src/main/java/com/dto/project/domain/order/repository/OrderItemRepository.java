package com.dto.project.domain.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dto.project.domain.order.entity.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long>{

}
