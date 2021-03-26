package com.example.tstradeengine.model;

import java.util.ArrayList;
import java.util.List;

public class EngineQueueRegister {
    public static List<EngineQueueItem> engineQueueRegister =  new ArrayList<>();


    static {
        engineQueueRegister.add(
                new EngineQueueItem(
                    new ExchangeOrder(new Order("MSFT", "BUY", 1.5, 5,1,true, 3),""),
                        5, 2,"incomplete"));
    engineQueueRegister.add(
                new EngineQueueItem(
                    new ExchangeOrder(new Order("MSFT", "SELL", 2.5, 5,1,true, 3),""),
                        5, 2,"incomplete"));

        engineQueueRegister.add(
                new EngineQueueItem(
                        new ExchangeOrder(new Order("NFLX", "BUY", 1.5, 5,1,true, 2),""),
                        3, 1,"incomplete"));

        engineQueueRegister.add(
                new EngineQueueItem(
                        new ExchangeOrder(new Order("NFLX", "SELL", 12.5, 5,1,true, 2),""),
                        3, 1,"incomplete"));

        engineQueueRegister.add(
                new EngineQueueItem(
                        new ExchangeOrder(new Order("GOOGL", "BUY", 1.5, 5,1,true, 2),""),
                        3, 1,"incomplete"));
        engineQueueRegister.add(
                new EngineQueueItem(
                        new ExchangeOrder(new Order("GOOGL", "SELL", 1.5, 5,1,true, 2),""),
                        3, 1,"incomplete"));

        engineQueueRegister.add(
                new EngineQueueItem(
                        new ExchangeOrder(new Order("AAPL", "BUY", 1.5, 5,1,true, 3),""),
                        5, 2,"incomplete"));
        engineQueueRegister.add(
                new EngineQueueItem(
                        new ExchangeOrder(new Order("IBM", "SELL", 1.5, 5,1,true, 3),""),
                        5, 2,"incomplete"));

        engineQueueRegister.add(
                new EngineQueueItem(
                        new ExchangeOrder(new Order("AAPL", "SELL", 1.5, 5,1,true, 2),""),
                        3, 1,"incomplete"));
        engineQueueRegister.add(
                new EngineQueueItem(
                        new ExchangeOrder(new Order("IBM", "BUY", 1.5, 5,1,true, 2),""),
                        3, 1,"incomplete"));

        engineQueueRegister.add(
                new EngineQueueItem(
                        new ExchangeOrder(new Order("ORCL", "BUY", 1.5, 5,1,true, 2),""),
                        3, 1,"incomplete"));

        engineQueueRegister.add(
                new EngineQueueItem(
                        new ExchangeOrder(new Order("AMZN", "BUY", 1.5, 5,1,true, 2),""),
                        3, 1,"incomplete"));
        engineQueueRegister.add(
                new EngineQueueItem(
                        new ExchangeOrder(new Order("AMZN", "SELL", 1.5, 5,1,true, 2),""),
                        3, 1,"incomplete"));

        engineQueueRegister.add(
                new EngineQueueItem(
                        new ExchangeOrder(new Order("ORCL", "SELL", 1.5, 5,1,true, 2),""),
                        3, 1,"incomplete"));
    }

    public EngineQueueRegister() {
    }

    public static List<EngineQueueItem> getEngineQueueRegister() {
        return engineQueueRegister;
    }

    public static void setEngineQueueRegister(List<EngineQueueItem> engineQueueRegister) {
        EngineQueueRegister.engineQueueRegister = engineQueueRegister;
    }
    public static void mergeEngineQueueRegister(List<EngineQueueItem> engineQueueRegister) {
        EngineQueueRegister.engineQueueRegister.addAll(engineQueueRegister);
    }
}
