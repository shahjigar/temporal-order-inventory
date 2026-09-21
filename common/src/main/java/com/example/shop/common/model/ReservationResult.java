package com.example.shop.common.model;

public class ReservationResult {
    private String reservationId;
    private String sku;
    private int quantity;
    private int remainingStock;

    public ReservationResult() {
    }

    public ReservationResult(String reservationId, String sku, int quantity, int remainingStock) {
        this.reservationId = reservationId;
        this.sku = sku;
        this.quantity = quantity;
        this.remainingStock = remainingStock;
    }

    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
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

    public int getRemainingStock() {
        return remainingStock;
    }

    public void setRemainingStock(int remainingStock) {
        this.remainingStock = remainingStock;
    }
}
