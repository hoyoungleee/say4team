package com.playdata.orderingservice.ordering.repository;

import com.playdata.orderingservice.ordering.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    // List<OrderItem> findByOrderId(Long orderId);
}
