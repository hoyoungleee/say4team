package com.playdata.orderingservice.ordering.repository;

import com.playdata.orderingservice.ordering.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    // Optional<Order> findByUserId(Long userId);

}
