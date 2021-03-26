package com.example.tstradeengine.model;

public class TradeEngineActivity {
    private long id;
    private String status;
    private String exchange;
    private long orderId;
    private String comment;

    public TradeEngineActivity(String status, String exchange, String comment, long orderId) {
        this.status = status;
        this.exchange = exchange;
        this.orderId = orderId;
        this.comment = comment;
    }

    public TradeEngineActivity() {
    }

    public String getStatus() {
        return status;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public long getOrderId() {
        return orderId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }
}
