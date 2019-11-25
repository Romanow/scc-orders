package ru.romanow.orders.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.romanow.orders.domain.Order;

import java.util.Optional;
import java.util.UUID;

public interface OrdersRepository
        extends JpaRepository<Order, Integer> {
    Optional<Order> findByUid(UUID orderUid);
}
