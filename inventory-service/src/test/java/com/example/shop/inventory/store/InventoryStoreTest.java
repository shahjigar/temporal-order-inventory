package com.example.shop.inventory.store;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class InventoryStoreTest {

    @Test
    void reservesAndReleasesHardcodedStock() {
        InventoryStore store = new InventoryStore();

        InventoryStore.Reservation reservation = store.hold("WIDGET-RED", 3);
        assertThat(reservation.remainingStock()).isEqualTo(7);
        assertThat(store.find("WIDGET-RED").orElseThrow().getQuantity()).isEqualTo(7);

        store.release(reservation.reservationId());
        assertThat(store.find("WIDGET-RED").orElseThrow().getQuantity()).isEqualTo(10);
    }

    @Test
    void rejectsWhenStockIsTooLow() {
        InventoryStore store = new InventoryStore();
        assertThatThrownBy(() -> store.hold("GADGET-1", 5)).isInstanceOf(InsufficientStockException.class);
    }
}
