package com.example.tstradeengine.model;

import javax.persistence.*;


public class Order {
    private long id;
    private String product;
    private String side;
    private Double price;
    private int quantity;
    private long clientId;
    private boolean isValid;
    private int strategy;

    public Order() {
    }

    public Order(String product, String side, Double price, int quantity, long clientId, boolean isValid, int strategy) {
        this.product = product;
        this.side = side;
        this.price = price;
        this.quantity = quantity;
        this.clientId = clientId;
        this.isValid = isValid;
        this.strategy = strategy;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getSide() {
        return side;
    }

    public void setSide(String side) {
        this.side = side;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public long getClientId() {
        return clientId;
    }

    public void setClientId(long clientId) {
        this.clientId = clientId;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

    public int getStrategy() {
        return strategy;
    }

    public void setStrategy(int strategy) {
        this.strategy = strategy;
    }

    @Override
    public String toString() {
        return "***Order{" +
                "id=" + id +
                ", product='" + product + '\'' +
                ", side='" + side + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                ", clientId=" + clientId +
                ", isValid=" + isValid +
                ", strategy=" + strategy +
                "}***";
    }
}
