package com.example.shop.common.workflow;

import com.example.shop.common.model.CustomerOrder;
import com.example.shop.common.model.Order;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface PlaceOrderWorkflow {

    @WorkflowMethod
    Order placeOrder(CustomerOrder request);
}
