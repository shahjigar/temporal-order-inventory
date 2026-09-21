package com.example.shop.inventory.store;

import com.example.shop.common.model.Item;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class InventoryStore {

    private final Map<String, Item> catalog = new ConcurrentHashMap<>();
    private final Map<String, HeldStock> holds = new ConcurrentHashMap<>();

    public InventoryStore() {
        catalog.put("WIDGET-RED", new Item("WIDGET-RED", "Red widget", 10));
        catalog.put("WIDGET-BLUE", new Item("WIDGET-BLUE", "Blue widget", 5));
        catalog.put("GADGET-1", new Item("GADGET-1", "Gadget", 2));
    }

    public List<Item> list() {
        return catalog.values().stream()
                .map(item -> new Item(item.getSku(), item.getName(), item.getQuantity()))
                .toList();
    }

    public Optional<Item> find(String sku) {
        Item item = catalog.get(sku);
        if (item == null) {
            return Optional.empty();
        }
        return Optional.of(new Item(item.getSku(), item.getName(), item.getQuantity()));
    }

    public synchronized Reservation hold(String sku, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        Item item = catalog.get(sku);
        if (item == null) {
            throw new UnknownSkuException(sku);
        }
        if (item.getQuantity() < quantity) {
            throw new InsufficientStockException(sku, quantity, item.getQuantity());
        }
        item.setQuantity(item.getQuantity() - quantity);
        String reservationId = UUID.randomUUID().toString();
        holds.put(reservationId, new HeldStock(sku, quantity));
        return new Reservation(reservationId, sku, quantity, item.getQuantity());
    }

    public synchronized void release(String reservationId) {
        HeldStock held = holds.remove(reservationId);
        if (held == null) {
            return;
        }
        Item item = catalog.get(held.sku());
        if (item != null) {
            item.setQuantity(item.getQuantity() + held.quantity());
        }
    }

    public record Reservation(String reservationId, String sku, int quantity, int remainingStock) {
    }

    private record HeldStock(String sku, int quantity) {
    }
}
