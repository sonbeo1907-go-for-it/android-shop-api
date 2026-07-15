package com.example.android_shop_api.repository.order;

import com.example.android_shop_api.entity.order.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository
        extends JpaRepository<Order, Long> {

    boolean existsByOrderCodeIgnoreCase(
            String orderCode
    );

    @EntityGraph(attributePaths = "items")
    Optional<Order> findByOrderCodeIgnoreCaseAndPhoneNumber(
            String orderCode,
            String phoneNumber
    );
}