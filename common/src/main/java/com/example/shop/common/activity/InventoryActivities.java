package com.example.shop.common.activity;

import com.example.shop.common.model.ReservationResult;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface InventoryActivities {

    @ActivityMethod
    ReservationResult reserve(String sku, int quantity);

    @ActivityMethod
    void release(String reservationId);
}
