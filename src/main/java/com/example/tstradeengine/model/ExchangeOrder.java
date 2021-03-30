package com.example.tstradeengine.model;

public class ExchangeOrder {
    private Order order;
    private String exchange;

    public ExchangeOrder(Order order, String exchange) {
        this.order = order;
        this.exchange = exchange;
    }

    public ExchangeOrder() {
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }
}
