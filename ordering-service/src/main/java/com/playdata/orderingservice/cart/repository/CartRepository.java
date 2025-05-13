package com.playdata.orderingservice.cart.repository;

import com.playdata.orderingservice.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByEmail(String email);
}
