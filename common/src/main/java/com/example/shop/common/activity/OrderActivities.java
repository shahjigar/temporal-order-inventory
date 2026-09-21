package com.example.shop.common.activity;

import com.example.shop.common.model.CustomerOrder;
import com.example.shop.common.model.Order;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface OrderActivities {

    @ActivityMethod
    Order recordOrder(CustomerOrder request, String reservationId);
}
