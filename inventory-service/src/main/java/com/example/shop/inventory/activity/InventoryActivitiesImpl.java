package com.example.shop.inventory.activity;

import com.example.shop.common.TaskQueues;
import com.example.shop.common.activity.InventoryActivities;
import com.example.shop.common.model.ReservationResult;
import com.example.shop.inventory.store.InsufficientStockException;
import com.example.shop.inventory.store.InventoryStore;
import com.example.shop.inventory.store.UnknownSkuException;
import io.temporal.failure.ApplicationFailure;
import io.temporal.spring.boot.ActivityImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@ActivityImpl(taskQueues = TaskQueues.INVENTORY)
public class InventoryActivitiesImpl implements InventoryActivities {
    private static final Logger log = LoggerFactory.getLogger(InventoryActivitiesImpl.class);

    private final InventoryStore store;

    public InventoryActivitiesImpl(InventoryStore store) {
        this.store = store;
    }

    @Override
    public ReservationResult reserve(String sku, int quantity) {
        try {
            InventoryStore.Reservation reservation = store.hold(sku, quantity);
            log.info(
                    "Reserved {} of {} (remaining {}) as {}",
                    quantity,
                    sku,
                    reservation.remainingStock(),
                    reservation.reservationId());
            return new ReservationResult(
                    reservation.reservationId(),
                    reservation.sku(),
                    reservation.quantity(),
                    reservation.remainingStock());
        } catch (InsufficientStockException | UnknownSkuException | IllegalArgumentException e) {
            throw ApplicationFailure.newNonRetryableFailure(e.getMessage(), e.getClass().getSimpleName());
        }
    }

    @Override
    public void release(String reservationId) {
        log.info("Releasing reservation {}", reservationId);
        store.release(reservationId);
    }
}
