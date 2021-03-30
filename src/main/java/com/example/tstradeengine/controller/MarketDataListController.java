package com.example.tstradeengine.controller;

import com.example.tstradeengine.model.MarketData;
import com.example.tstradeengine.model.MarketDataList;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MarketDataListController {
    @GetMapping("v0/api/md-by-ticker/{ticker}")
    public List<MarketData> getMarketDataByTicker(@PathVariable(name="ticker") String ticker) {
        System.out.println("******************************* called mark data for "+ticker);
        return MarketDataList.getMarketDataListByTicker(ticker);
    }

    @GetMapping("v0/api/market-data")
    public List<MarketData> getAllMarketData() {
        return MarketDataList.getMarketDataList();
    }
}
