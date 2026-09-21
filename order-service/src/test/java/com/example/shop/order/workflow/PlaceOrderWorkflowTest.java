package com.example.shop.order.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.shop.common.TaskQueues;
import com.example.shop.common.activity.InventoryActivities;
import com.example.shop.common.activity.OrderActivities;
import com.example.shop.common.model.CustomerOrder;
import com.example.shop.common.model.Order;
import com.example.shop.common.model.ReservationResult;
import com.example.shop.common.workflow.PlaceOrderWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowFailedException;
import io.temporal.client.WorkflowOptions;
import io.temporal.failure.ApplicationFailure;
import io.temporal.testing.TestWorkflowEnvironment;
import io.temporal.worker.Worker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class PlaceOrderWorkflowTest {
    private TestWorkflowEnvironment env;

    @AfterEach
    void tearDown() {
        if (env != null) {
            env.close();
        }
    }

    @Test
    void reservesInventoryThenPlacesOrder() {
        FakeInventory inventory = new FakeInventory(10);
        PlaceOrderWorkflow workflow = start(inventory, new InMemoryOrders());

        Order result = workflow.placeOrder(new CustomerOrder("cust-1", "WIDGET-RED", 3));

        assertThat(result.getStatus()).isEqualTo("PLACED");
        assertThat(result.getSku()).isEqualTo("WIDGET-RED");
        assertThat(result.getQuantity()).isEqualTo(3);
        assertThat(inventory.stock).isEqualTo(7);
    }

    @Test
    void failsWithoutRetryWhenStockIsInsufficient() {
        FakeInventory inventory = new FakeInventory(10);
        PlaceOrderWorkflow workflow = start(inventory, new InMemoryOrders());

        assertThatThrownBy(() -> workflow.placeOrder(new CustomerOrder("cust-1", "WIDGET-RED", 99)))
                .isInstanceOf(WorkflowFailedException.class);
        assertThat(inventory.stock).isEqualTo(10);
    }

    @Test
    void releasesReservationWhenRecordingTheOrderFails() {
        FakeInventory inventory = new FakeInventory(10);
        PlaceOrderWorkflow workflow = start(inventory, new FailingOrders());

        assertThatThrownBy(() -> workflow.placeOrder(new CustomerOrder("cust-1", "WIDGET-RED", 2)))
                .isInstanceOf(WorkflowFailedException.class);
        assertThat(inventory.stock).isEqualTo(10);
    }

    private PlaceOrderWorkflow start(InventoryActivities inventory, OrderActivities orders) {
        env = TestWorkflowEnvironment.newInstance();
        WorkflowClient client = env.getWorkflowClient();

        Worker orderWorker = env.newWorker(TaskQueues.ORDER);
        orderWorker.registerWorkflowImplementationTypes(PlaceOrderWorkflowImpl.class);
        orderWorker.registerActivitiesImplementations(orders);

        Worker inventoryWorker = env.newWorker(TaskQueues.INVENTORY);
        inventoryWorker.registerActivitiesImplementations(inventory);

        env.start();
        return client.newWorkflowStub(
                PlaceOrderWorkflow.class,
                WorkflowOptions.newBuilder().setTaskQueue(TaskQueues.ORDER).build());
    }

    private static final class FakeInventory implements InventoryActivities {
        private int stock;
        private int held;

        private FakeInventory(int stock) {
            this.stock = stock;
        }

        @Override
        public ReservationResult reserve(String sku, int quantity) {
            if (quantity > stock) {
                throw ApplicationFailure.newNonRetryableFailure(
                        "Insufficient stock for " + sku, "InsufficientStockException");
            }
            stock -= quantity;
            held = quantity;
            return new ReservationResult("res-1", sku, quantity, stock);
        }

        @Override
        public void release(String reservationId) {
            stock += held;
            held = 0;
        }
    }

    private static final class InMemoryOrders implements OrderActivities {
        @Override
        public Order recordOrder(CustomerOrder request, String reservationId) {
            return new Order(
                    "ord-1",
                    reservationId,
                    request.getCustomerId(),
                    request.getSku(),
                    request.getQuantity(),
                    "PLACED");
        }
    }

    private static final class FailingOrders implements OrderActivities {
        @Override
        public Order recordOrder(CustomerOrder request, String reservationId) {
            throw ApplicationFailure.newNonRetryableFailure("cannot persist order", "OrderStoreException");
        }
    }
}
