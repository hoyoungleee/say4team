package com.playdata.orderingservice.ordering.repository;

import com.playdata.orderingservice.ordering.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    // 이메일로 주문을 찾도록
    List<Order> findAllByEmail(String email);
}