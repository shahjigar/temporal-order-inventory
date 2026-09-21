package com.example.shop.order.workflow;

import com.example.shop.common.TaskQueues;
import com.example.shop.common.activity.InventoryActivities;
import com.example.shop.common.activity.OrderActivities;
import com.example.shop.common.model.CustomerOrder;
import com.example.shop.common.model.Order;
import com.example.shop.common.model.ReservationResult;
import com.example.shop.common.workflow.PlaceOrderWorkflow;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.failure.ActivityFailure;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;
import java.time.Duration;
import org.slf4j.Logger;

@WorkflowImpl(taskQueues = TaskQueues.ORDER)
public class PlaceOrderWorkflowImpl implements PlaceOrderWorkflow {
    private static final Logger log = Workflow.getLogger(PlaceOrderWorkflowImpl.class);

    private final InventoryActivities inventory = Workflow.newActivityStub(
            InventoryActivities.class,
            ActivityOptions.newBuilder()
                    .setTaskQueue(TaskQueues.INVENTORY)
                    .setStartToCloseTimeout(Duration.ofSeconds(10))
                    .setRetryOptions(RetryOptions.newBuilder()
                            .setMaximumAttempts(3)
                            .setDoNotRetry("InsufficientStockException", "UnknownSkuException", "IllegalArgumentException")
                            .build())
                    .build());

    private final OrderActivities orders = Workflow.newActivityStub(
            OrderActivities.class,
            ActivityOptions.newBuilder()
                    .setTaskQueue(TaskQueues.ORDER)
                    .setStartToCloseTimeout(Duration.ofSeconds(10))
                    .setRetryOptions(RetryOptions.newBuilder().setMaximumAttempts(3).build())
                    .build());

    @Override
    public Order placeOrder(CustomerOrder request) {
        log.info("Placing order for {} x {}", request.getQuantity(), request.getSku());
        ReservationResult reservation = inventory.reserve(request.getSku(), request.getQuantity());
        try {
            return orders.recordOrder(request, reservation.getReservationId());
        } catch (ActivityFailure e) {
            log.warn("Order recording failed; releasing reservation {}", reservation.getReservationId());
            inventory.release(reservation.getReservationId());
            throw e;
        }
    }
}
