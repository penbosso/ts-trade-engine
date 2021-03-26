package com.example.tstradeengine.service;

import com.example.tstradeengine.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TradeEngineService {

    // Strategy is by low sell high
    // risk level(long term, mid, short term)
    // signal: if change in price is greater than 1, buy if lesser than 1 sell
    // determine exchange
    // am using res to determine thr algo
    // use signal to execute
    // we check client profile for risk and the market data for signal
    // the pass/ isReady
    // status: exchanged / held / canceled

    @Autowired
    private RedisQueueService redisQueueService;

    @Autowired
    private EngineQueueItemRepository engineQueueItemRepository;


    // iteration execution
    public void processTradeQueue() { System.out.println("+=========================Process Trade Queue ===================================");
            List<EngineQueueItem> tempList = new ArrayList<>();
            tempList.addAll(EngineQueueRegister.getEngineQueueRegister());
        if(!EngineQueueRegister.getEngineQueueRegister().isEmpty()){
            int tempListSize = EngineQueueRegister.getEngineQueueRegister().size();
            System.out.println("+======tempListSize:::==========="+tempListSize+"========= ->"+tempListSize+"<-  ==============="+tempListSize+"====================");

            for (EngineQueueItem queueItem: tempList) {
//                EngineQueueItem queueItem = EngineQueueRegister.getEngineQueueRegister().remove(i);
                System.out.println("+==========================boolean ->"+EngineQueueRegister.getEngineQueueRegister().remove(queueItem)+"<-  ===================================");

                evaluateTradeOrder(queueItem.getOrder(), queueItem.getPatience(), queueItem.getSignal());

            }
        } else {
            System.out.println("+==========================No trade to execute ===================================");
        }
    }

    // (ex1[lt:1, bid:1: ask1) (ex2(lt=2, bid:5, ask:2) good to sell at ex2 buy at ex1

    public void tradeOrder(Order order) {
        if (order.getStrategy() == 1 || order.getStrategy() == 4) {
            List<MarketData> productMarketDataList = MarketDataList.getMarketDataListByTicker(order.getProduct());
            MarketData marketData;
            String comment = "";
            // checking pricing at market
            // (ex1[lt:1, bid:1: ask1) (ex2(lt=2, bid:5, ask:2) good to sell at ex2 buy at ex1
            //if buying or selling
            if (order.getSide() == "BUY") {
                // we need to buy at a list price . let's use the market/exchange with least askPrice
                marketData = minAskPriceMarketData(productMarketDataList);
                comment = "market with least ask price";
            } else {
                marketData = maxBidPriceMarketData(productMarketDataList);
                comment = "market with max bid price";
                //we could also check whether the is a higher asked price at the other exchanges (left for strategy 2)
            }

            ExchangeOrder exchangeOrder = new ExchangeOrder(order, marketData.getExchangeUrl());
            toExchange(exchangeOrder);
            if(order.getStrategy() == 4)
                comment += " patience limit reached";
            toReport(order.getId(),"exchanged",marketData.getExchangeUrl(), comment);
        }

        // do use patience, signal and price adjustment
        if (order.getStrategy() == 2) {
            int patience = 3;
            double signal = 1;
            evaluateTradeOrder(order, patience, signal);
        }

        // do use higher ^patience, ^signal and ^price adjustment
        if (order.getStrategy() == 3) {
            int patience = 5;
            double signal = 2;
            List<MarketData> productMarketDataList = MarketDataList.getMarketDataListByTicker(order.getProduct());
            // checking pricing at market
            evaluateTradeOrder(order, patience, signal);
        }
    }

    private void evaluateTradeOrder(Order order, int patience, double signal) {
        System.out.println("+=====Evaluating trade"+order.getProduct()+" : "+order.getSide()+"with Signal ="+signal+"===Patience="+patience+"=======");
        List<MarketData> productMarketDataList = MarketDataList.getMarketDataListByTicker(order.getProduct());

        //decrease patience
        patience -= patience;
        if(patience < 0) {
            // when patience is exhausted evaluate trade with short term strategy
            order.setStrategy(4);
            System.out.println("+=====Patienc reduce te 0 stategy set to 4"+order.getProduct()+" : "+order.getSide()+"with Signal ="+signal+"===Patience="+patience+"=======");
            tradeOrder(order);
        } else if (order.getSide() == "BUY") {
            System.out.println("+=====Buying branch"+order.getProduct()+" : "+order.getSide()+"with Signal ="+signal+"===Patience="+patience+"=======");
            //if buying or selling
            evaluateBuyOrder(order, patience, signal, productMarketDataList);
        } else {
            System.out.println("+=====Selling branch"+order.getProduct()+" : "+order.getSide()+"with Signal ="+signal+"===Patience="+patience+"=======");
            evaluateSellOrder(order, patience, signal, productMarketDataList);
        }
    }

    private void evaluateSellOrder(Order order, int patience, double signal, List<MarketData> productMarketDataList) {
        // sell if price is falling
        MarketData marketData = maxBidPriceMarketData(productMarketDataList);
        ExchangeOrder exchangeOrder = new ExchangeOrder(order, marketData.getExchangeUrl());
        String argComment ="";
        if (marketData.getChangeInPrice() < -signal) {
            // good to sell
            //modify price to cell at max price bid price or last traded price
            // to be experimented sell as quick as possible
            if(order.getPrice() < marketData.getBID_PRICE()) {
                order.setPrice(marketData.getBID_PRICE());
                argComment ="better bid price";
            } else {
                order.setPrice(marketData.getLAST_TRADED_PRICE());
                argComment = "ask price adjusted to last bid price";
            }
            toExchange(exchangeOrder);
            toReport(order.getId(),"exchanged",marketData.getExchangeUrl(),
                    "Signal condition : -" + signal +" met at patience "+ patience+" "+argComment);
            engineQueueItemRepository.save(new EngineQueueItem(exchangeOrder, patience, signal, "completed"));
        } else {
            toReport(order.getId(),"exchanged","Best exchange : "+marketData.getExchangeUrl(),
                    "Signal condition : "+ signal +" not met at patience "+ patience);
            // save with status and
            // Queue order and wait for change in market data
            EngineQueueItem engineQueueItem = engineQueueItemRepository.save(new EngineQueueItem(exchangeOrder, patience, signal, "incomplete"));
            EngineQueueRegister.engineQueueRegister.add(engineQueueItem);
        }
    }

    private void evaluateBuyOrder(Order order, int patience, double signal, List<MarketData> productMarketDataList) {
        // we need to buy when price is increasing
        MarketData marketData = minAskPriceMarketData(productMarketDataList);
        ExchangeOrder exchangeOrder = new ExchangeOrder(order, marketData.getExchangeUrl());
        String argComment ="";
        // thus product price is increasing
        if (marketData.getChangeInPrice() > signal) {
            // good to buy
            // to be experimented; buy as quick as possible
            if(order.getPrice() < marketData.getASK_PRICE()) {
                order.setPrice(marketData.getASK_PRICE());
                argComment ="better asked price";
            } else {
                order.setPrice(marketData.getLAST_TRADED_PRICE());
                argComment = "did price adjusted to last bid price";
            }
            toExchange(exchangeOrder);
            toReport(order.getId(),"exchanged",marketData.getExchangeUrl(),
                    "Signal condition : +" + signal +" met at patience "+ patience+" "+argComment);
            engineQueueItemRepository.save(new EngineQueueItem(exchangeOrder, patience, signal, "completed"));
        } else {

            toReport(order.getId(),"exchanged","Best exchange : "+marketData.getExchangeUrl(),
                    "Signal condition : "+ signal +" not met at patience "+ patience);
            // save with status and
            // Queue order and wait for change in market data
            EngineQueueItem engineQueueItem = engineQueueItemRepository.save(new EngineQueueItem(exchangeOrder, patience, signal, "incomplete"));
            EngineQueueRegister.getEngineQueueRegister().add(engineQueueItem);
        }
    }

    private void toReport(long orderId, String status, String exchangeUrl, String comment) {
        // report activity
        redisQueueService.sendReportOrderToQueue(
                new TradeEngineActivity(status, exchangeUrl, comment, orderId));
    }

    private void toExchange(ExchangeOrder exchangeOrder) {
        // send to Exchange
        redisQueueService.sendOrderToQueue(exchangeOrder);
    }


    //BID_PRICE -the last, maximum price someone was prepared
    // to pay for the stock
    // The bigger this value the best for selling
    // (i.e if this min value is greater than that of other exchanges)
    private MarketData maxBidPriceMarketData(List<MarketData> productMarketDataList) {
        MarketData maxPriceMd = productMarketDataList.get(0);
        for (MarketData md : productMarketDataList) {
            if (md.getBID_PRICE() > maxPriceMd.getBID_PRICE()) {
                maxPriceMd = md;
            }
        }
        return maxPriceMd;
    }

    // The smaller this value the best for buying
    // (i.e if this value is lesser than that of other exchanges)
    // ASK_PRICE - the last, minimum price someone was
    // prepared to sell their holding of stock for
    private MarketData minAskPriceMarketData(List<MarketData> productMarketDataList) {
        MarketData minPriceMd = productMarketDataList.get(0);
        for (MarketData md : productMarketDataList) {
            if (md.getASK_PRICE() < minPriceMd.getASK_PRICE()) {
                minPriceMd = md;
            }
        }
        return minPriceMd;
    }
}
