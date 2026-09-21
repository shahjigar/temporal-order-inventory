package com.example.shop.inventory.api;

import com.example.shop.common.model.Item;
import com.example.shop.inventory.store.InventoryStore;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {
    private final InventoryStore store;

    public InventoryController(InventoryStore store) {
        this.store = store;
    }

    @GetMapping
    public List<Item> list() {
        return store.list();
    }

    @GetMapping("/{sku}")
    public Item get(@PathVariable String sku) {
        return store.find(sku)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown SKU: " + sku));
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "inventory-service");
    }
}
