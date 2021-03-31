package com.example.tstradeengine.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = false)
public class MarketData {
    @JsonProperty(value="SELL_LIMIT")
    int SELL_LIMIT;
    @JsonProperty(value="LAST_TRADED_PRICE")
    double LAST_TRADED_PRICE;
    @JsonProperty(value="ASK_PRICE")
    double ASK_PRICE;
    @JsonProperty(value="BUY_LIMIT")
    double BUY_LIMIT;
    @JsonProperty(value="BID_PRICE")
    double BID_PRICE;
    @JsonProperty(value="TICKER")
    String TICKER;
    @JsonProperty(value="MAX_PRICE_SHIFT")
    double MAX_PRICE_SHIFT;
    String exchangeUrl;
    double changeInPrice = 0;

    public String getExchangeUrl() {
        return exchangeUrl;
    }

    public void setExchangeUrl(String exchangeUrl) {
        this.exchangeUrl = exchangeUrl;
    }

    public Double getChangeInPrice() {
        return changeInPrice;
    }

    public void setChangeInPrice(Double changeInPrice) {
        this.changeInPrice = changeInPrice;
    }

    public int getSELL_LIMIT() {
        return SELL_LIMIT;
    }

    public void setSELL_LIMIT(int SELL_LIMIT) {
        this.SELL_LIMIT = SELL_LIMIT;
    }

    public double getLAST_TRADED_PRICE() {
        return LAST_TRADED_PRICE;
    }

    public void setLAST_TRADED_PRICE(double LAST_TRADED_PRICE) {
        this.LAST_TRADED_PRICE = LAST_TRADED_PRICE;
    }

    public double getASK_PRICE() {
        return ASK_PRICE;
    }

    public void setASK_PRICE(double ASK_PRICE) {
        this.ASK_PRICE = ASK_PRICE;
    }

    public double getBUY_LIMIT() {
        return BUY_LIMIT;
    }

    public void setBUY_LIMIT(double BUY_LIMIT) {
        this.BUY_LIMIT = BUY_LIMIT;
    }

    public double getBID_PRICE() {
        return BID_PRICE;
    }

    public void setBID_PRICE(double BID_PRICE) {
        this.BID_PRICE = BID_PRICE;
    }

    public String getTICKER() {
        return TICKER;
    }

    public void setTICKER(String TICKER) {
        this.TICKER = TICKER;
    }

    public double getMAX_PRICE_SHIFT() {
        return MAX_PRICE_SHIFT;
    }

    public void setMAX_PRICE_SHIFT(double MAX_PRICE_SHIFT) {
        this.MAX_PRICE_SHIFT = MAX_PRICE_SHIFT;
    }

    @Override
    public String toString() {
        return "*********MarketData{" +
                "SELL_LIMIT=" + SELL_LIMIT +
                ", LAST_TRADED_PRICE=" + LAST_TRADED_PRICE +
                ", ASK_PRICE=" + ASK_PRICE +
                ", BUY_LIMIT=" + BUY_LIMIT +
                ", BID_PRICE=" + BID_PRICE +
                ", TICKER='" + TICKER + '\'' +
                ", MAX_PRICE_SHIFT=" + MAX_PRICE_SHIFT +
                "}*********";
    }
}
