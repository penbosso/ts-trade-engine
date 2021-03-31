package com.example.tstradeengine.model;

import javax.persistence.*;

@Entity
public class EngineQueueItem {
    @Id
    @GeneratedValue
    private long id;
    private int patience;
    private double signal;
    private String status;
    private String exchangeUrl;

    public EngineQueueItem() {
    }

    public EngineQueueItem(ExchangeOrder exchangeOrder, int patience, double signal, String status) {
        this.exchangeUrl = exchangeOrder.getExchange();
        setOrder(exchangeOrder.getOrder());
        this.patience = patience;
        this.signal = signal;
        this.status = status;
    }

    public ExchangeOrder getExchangeOrder() {
        return new ExchangeOrder(getOrder(), exchangeUrl);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setExchangeOrder(ExchangeOrder exchangeOrder) {
        this.exchangeUrl = exchangeOrder.getExchange();
        setOrder(exchangeOrder.getOrder());
    }

    public int getPatience() {
        return patience;
    }

    public void setPatience(int patience) {
        this.patience = patience;
    }

    public double getSignal() {
        return signal;
    }

    public void setSignal(double signal) {
        this.signal = signal;
    }
    // order atr

    private long orderId;
    private String product;
    private String side;
    private Double price;
    private int quantity;
    private long clientId;
    private boolean isValid;
    private int strategy;
    // construct order and remove order field


    public Order getOrder() {
        Order obj = new Order(product, side, price, quantity, clientId, isValid, strategy);
        obj.setId(orderId);
        return obj;
    }

    public void setOrder(Order order) {
        this.orderId = order.getId();
        this.product = order.getProduct();
        this.side = order.getSide();
        this.quantity = order.getQuantity();
        this.price = order.getPrice();
        this.clientId = order.getClientId();
        this.isValid = order.isValid();
        this.strategy = order.getStrategy();
    }

    @Override
    public String toString() {
        return "EngineQueueItem{" +
                "id=" + id +
                ", patience=" + patience +
                ", signal=" + signal +
                ", status='" + status + '\'' +
                ", exchangeUrl='" + exchangeUrl + '\'' +
                ", orderId=" + orderId +
                ", product='" + product + '\'' +
                ", side='" + side + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                ", clientId=" + clientId +
                ", isValid=" + isValid +
                ", strategy=" + strategy +
                '}';
    }
}
