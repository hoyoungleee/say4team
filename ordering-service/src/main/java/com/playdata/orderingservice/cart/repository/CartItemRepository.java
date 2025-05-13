package com.playdata.orderingservice.cart.repository;

import com.playdata.orderingservice.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}
