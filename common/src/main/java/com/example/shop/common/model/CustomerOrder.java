package com.example.shop.common.model;

public class CustomerOrder {
    private String customerId;
    private String sku;
    private int quantity;

    public CustomerOrder() {
    }

    public CustomerOrder(String customerId, String sku, int quantity) {
        this.customerId = customerId;
        this.sku = sku;
        this.quantity = quantity;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
