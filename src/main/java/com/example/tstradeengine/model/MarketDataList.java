package com.example.tstradeengine.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MarketDataList {
    static List<MarketData> marketDataList = new ArrayList<>();

    public MarketDataList(List<MarketData> marketDataList) {
        MarketDataList.marketDataList = marketDataList;
    }

    public MarketDataList() {
    }

    public static List<MarketData> getMarketDataListByTicker(String ticker) {
        return marketDataList.stream()
                .filter(marketData -> marketData.getTICKER()
                        .equals(ticker))
                .collect(Collectors.toList());
    }

    public static MarketData getMarketDataListByTickerAndUrl(String ticker, String exchangeUrl) {
        List<MarketData> tempList =MarketDataList.getMarketDataListByTicker(ticker);
        return  tempList.stream().filter(marketData -> marketData.getExchangeUrl().equals(exchangeUrl))
                .collect(Collectors.toList()).get(0);
    }

    public static List<MarketData> getMarketDataListByUrl(String exchangeUrl) {
        return marketDataList.stream()
                .filter(marketData -> marketData.getExchangeUrl()
                        .equals(exchangeUrl))
                .collect(Collectors.toList());
    }

    public static void merchData(List<MarketData> dataList, String exchangeUrl) {
        if (MarketDataList.marketDataList.isEmpty()) {
            MarketDataList.marketDataList = dataList;
        } else {
            MarketDataList.marketDataList = MarketDataList.marketDataList.stream()
                    .filter(marketData -> !(marketData.getExchangeUrl().equals(exchangeUrl)))
                    .collect(Collectors.toList());
            MarketDataList.marketDataList.addAll(dataList);
        }
    }

    public static List<MarketData> getMarketDataList() {
        return marketDataList;
    }

    public static void setMarketDataList(List<MarketData> marketDataList) {
        MarketDataList.marketDataList.clear();
        MarketDataList.marketDataList = marketDataList;
    }


}
