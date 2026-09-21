package com.example.shop.order.store;

import com.example.shop.common.model.Order;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class OrderStore {
    private final Map<String, Order> orders = new ConcurrentHashMap<>();

    public Order save(String customerId, String sku, int quantity, String reservationId) {
        String orderId = UUID.randomUUID().toString();
        Order order = new Order(orderId, reservationId, customerId, sku, quantity, "PLACED");
        orders.put(orderId, order);
        return order;
    }

    public List<Order> list() {
        return new ArrayList<>(orders.values());
    }

    public Optional<Order> find(String orderId) {
        return Optional.ofNullable(orders.get(orderId));
    }
}
