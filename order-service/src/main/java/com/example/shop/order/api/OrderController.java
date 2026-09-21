package com.example.shop.order.api;

import com.example.shop.common.TaskQueues;
import com.example.shop.common.model.CustomerOrder;
import com.example.shop.common.model.Order;
import com.example.shop.common.workflow.PlaceOrderWorkflow;
import com.example.shop.order.store.OrderStore;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowFailedException;
import io.temporal.client.WorkflowOptions;
import io.temporal.failure.ApplicationFailure;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class OrderController {
    private final WorkflowClient workflowClient;
    private final OrderStore store;

    public OrderController(WorkflowClient workflowClient, OrderStore store) {
        this.workflowClient = workflowClient;
        this.store = store;
    }

    @PostMapping("/orders")
    public ResponseEntity<?> placeOrder(@RequestBody CustomerOrder request) {
        if (request.getSku() == null || request.getSku().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "sku is required");
        }
        if (request.getQuantity() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantity must be positive");
        }

        String workflowId = "order-" + UUID.randomUUID();

        PlaceOrderWorkflow workflow = workflowClient.newWorkflowStub(
                PlaceOrderWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(TaskQueues.ORDER)
                        .setWorkflowId(workflowId)
                        .setWorkflowRunTimeout(Duration.ofSeconds(30))
                        .build());

        try {
            Order result = workflow.placeOrder(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (WorkflowFailedException e) {
            Throwable cause = e.getCause();
            while (cause != null) {
                if (cause instanceof ApplicationFailure failure) {
                    String type = failure.getType();
                    if ("InsufficientStockException".equals(type)) {
                        return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(Map.of("error", failure.getOriginalMessage(), "type", type));
                    }
                    if ("UnknownSkuException".equals(type)) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(Map.of("error", failure.getOriginalMessage(), "type", type));
                    }
                }
                cause = cause.getCause();
            }
            throw e;
        }
    }

    @GetMapping("/orders")
    public List<Order> list() {
        return store.list();
    }

    @GetMapping("/orders/{orderId}")
    public Order get(@PathVariable String orderId) {
        return store.find(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown order: " + orderId));
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "order-service");
    }
}
