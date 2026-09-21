package com.example.shop.common.model;

public class Order {
    private String orderId;
    private String reservationId;
    private String customerId;
    private String sku;
    private int quantity;
    private String status;

    public Order() {
    }

    public Order(
            String orderId,
            String reservationId,
            String customerId,
            String sku,
            int quantity,
            String status) {
        this.orderId = orderId;
        this.reservationId = reservationId;
        this.customerId = customerId;
        this.sku = sku;
        this.quantity = quantity;
        this.status = status;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
