package com.example.shop.order.activity;

import com.example.shop.common.TaskQueues;
import com.example.shop.common.activity.OrderActivities;
import com.example.shop.common.model.CustomerOrder;
import com.example.shop.common.model.Order;
import com.example.shop.order.store.OrderStore;
import io.temporal.spring.boot.ActivityImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@ActivityImpl(taskQueues = TaskQueues.ORDER)
public class OrderActivitiesImpl implements OrderActivities {
    private static final Logger log = LoggerFactory.getLogger(OrderActivitiesImpl.class);

    private final OrderStore store;

    public OrderActivitiesImpl(OrderStore store) {
        this.store = store;
    }

    @Override
    public Order recordOrder(CustomerOrder request, String reservationId) {
        Order order = store.save(
                request.getCustomerId(), request.getSku(), request.getQuantity(), reservationId);
        log.info("Recorded order {} for SKU {}", order.getOrderId(), order.getSku());
        return order;
    }
}
