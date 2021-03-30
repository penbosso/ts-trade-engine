package com.example.tstradeengine.service;

import com.example.tstradeengine.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

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
    // monitor change in price
    // strategy 0 just choose the best exchange
    // strategy 1 signal S 0.1 patience P 26 if P 0 trade on best exchange
    // strategy 2 signal S 0.2 patience 20 if P 0 fall to S1
    // strategy 3 signal S 0.3 patience 12 if P 0 fall to S2
    // strategy 4 signal S 0.5 patience 12 if P 0 fall to S3
    // strategy 5 signal S 0.8 patience 12 if P 0 fall to S4
    // strategy 6 signal S 1 patience 12 if P 0 fall to S5
    // strategy 7 signal S 2 patience 12 if P 0 fall to S6
    // strategy 8 signal S 3 patience 12 if P 0 fall to S7

    @Autowired
    private RedisQueueService redisQueueService;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Autowired
    private EngineQueueItemRepository engineQueueItemRepository;

    @Autowired
    RestTemplate restTemplate;


    // iteration execution
    public void processTradeQueue() { System.out.println("+=========================Process Trade Queue ===================================");
            List<EngineQueueItem> tempList = new ArrayList<>();
            tempList.addAll(EngineQueueRegister.getEngineQueueRegister());
        if(!EngineQueueRegister.getEngineQueueRegister().isEmpty()){
            int tempListSize = EngineQueueRegister.getEngineQueueRegister().size();
            System.out.println("+======tempListSize:::==========="+tempListSize+"========= ->"+tempListSize+"<-  ==============="+tempListSize+"====================");

            for (EngineQueueItem queueItem: tempList) {
//                EngineQueueItem queueItem = EngineQueueRegister.getEngineQueueRegister().remove(i);
                if(queueItem != null) {
                    evaluateTradeOrder(queueItem.getOrder(), queueItem.getPatience(), queueItem.getSignal());
                    System.out.println("+======Size before removing :::==========="+EngineQueueRegister.getEngineQueueRegister().size());
                    System.out.println("+==========================boolean ->"+EngineQueueRegister.getEngineQueueRegister().remove(queueItem)+"<-  ===================================");

                    System.out.println("+======Size after removing :::==========="+EngineQueueRegister.getEngineQueueRegister().size());
                } else {
                    System.out.println("+==========================.Null Pointer Dodged  ===============================");
                }
            }
            System.out.println("+==========================What's in queue ->"+EngineQueueRegister.getEngineQueueRegister()+"<-  ===================================");

        } else {
            System.out.println("+==========================No trade to execute ===================================");
        }
    }

    // (ex1[lt:1, bid:1: ask1) (ex2(lt=2, bid:5, ask:2) good to sell at ex2 buy at ex1

    public void tradeOrder(Order order) {
        if (order.getStrategy() == 0) {
            List<MarketData> productMarketDataList = MarketDataList.getMarketDataListByTicker(order.getProduct());
            MarketData marketData;
            String comment = "";
            // checking pricing at market
            // (ex1[lt:1, bid:1: ask1) (ex2(lt=2, bid:5, ask:2) good to sell at ex2 buy at ex1
            //if buying or selling
            if (order.getSide().equals("BUY")) {
                // we need to buy at a list price . let's use the market/exchange with least askPrice
                marketData = minAskPriceMarketData(productMarketDataList);
                comment = "market with least ask price";
                if (order.getPrice() < marketData.getLAST_TRADED_PRICE() - marketData.getMAX_PRICE_SHIFT() ||
                        order.getPrice() > marketData.getLAST_TRADED_PRICE() + marketData.getMAX_PRICE_SHIFT()) {
                    comment +=" due to max price shift, price has been adjusted";
                    if(order.getPrice() < marketData.getBID_PRICE()) {
                        System.out.println("+=====Adjusting price "+marketData.getBID_PRICE()+" : "+order.getSide()+" change in price ="+marketData.getChangeInPrice()+"=======");
                        order.setPrice(marketData.getBID_PRICE());
                    } else {
                        System.out.println("+=====Adjusting price last t price "+marketData.getLAST_TRADED_PRICE()+" : "+order.getSide()+"change in price ="+marketData.getChangeInPrice()+"=======");
                        order.setPrice(marketData.getLAST_TRADED_PRICE());
                    }
                }
            } else {
                marketData = maxBidPriceMarketData(productMarketDataList);
                comment = "market with max bid price";
                //we could also check whether the is a higher asked price at the other exchanges (left for strategy 2)
                if (order.getPrice() < marketData.getLAST_TRADED_PRICE() - marketData.getMAX_PRICE_SHIFT() ||
                        order.getPrice() > marketData.getLAST_TRADED_PRICE() + marketData.getMAX_PRICE_SHIFT()) {
                    comment +=" due to max price shift, price has been adjusted";
                    // to be experimented; buy as quick as possible
                    if(order.getPrice() < marketData.getASK_PRICE()) {
                        System.out.println("+=====Adjusting price "+marketData.getASK_PRICE()+" : "+order.getSide()+"change in price ="+marketData.getChangeInPrice()+"=======");
                        order.setPrice(marketData.getASK_PRICE());
                    } else {
                        System.out.println("+=====Adjusting price last t price "+marketData.getLAST_TRADED_PRICE()+" : "+order.getSide()+"change in price ="+marketData.getChangeInPrice()+"=======");
                        order.setPrice(marketData.getLAST_TRADED_PRICE());
                    }
                }
            }

            ExchangeOrder exchangeOrder = new ExchangeOrder(order, marketData.getExchangeUrl());
            toExchange(exchangeOrder);
            toReport(order.getId(),"exchanged",marketData.getExchangeUrl(), comment);
        }

        if(order.getStrategy() == 1 || order.getStrategy()==10) {
            int patience = 26;
            double signal = 0.1;
            String comment = "";
            List<MarketData> productMarketDataList = MarketDataList.getMarketDataListByTicker(order.getProduct());

            if(order.getStrategy() == 10){
                comment += " patience limit reached (strategy fallback)";
                String argComment ="";
                if (order.getSide().equals("SELL")) {
                    // to be experimented sell as quick as possible
                    MarketData marketData = maxBidPriceMarketData(productMarketDataList);
                    ExchangeOrder exchangeOrder = new ExchangeOrder(order, marketData.getExchangeUrl());
                    System.out.println("+=====Sell Signal condition fulfilled "+order.getProduct()+" : "+order.getSide()+ " change in price ="+marketData.getChangeInPrice()+"=======");
                    // good to sell
                    //modify price to cell at max price bid price or last traded price
                    // to be experimented sell as quick as possible
                    if(order.getPrice() < marketData.getBID_PRICE()) {
                        System.out.println("+=====Adjusting price "+marketData.getBID_PRICE()+" : "+order.getSide()+" change in price ="+marketData.getChangeInPrice()+"=======");
                        order.setPrice(marketData.getBID_PRICE());
                        argComment ="better bid price";
                    } else {
                        System.out.println("+=====Adjusting price last t price "+marketData.getLAST_TRADED_PRICE()+" : "+order.getSide()+"change in price ="+marketData.getChangeInPrice()+"=======");
                        order.setPrice(marketData.getLAST_TRADED_PRICE());
                        argComment = "ask price adjusted to last bid price";
                    }
                    System.out.println("+=====Sending to exchange and report ========");

                    toExchange(exchangeOrder);
                    toReport(order.getId(),"exchanged",marketData.getExchangeUrl(),
                            "Signal condition : -" + signal +" met at patience "+ patience+" "+argComment);
                    String status = "completed";
                    saveOrUpdateQueueItem(patience, signal, exchangeOrder, status);
                    System.out.println("+==========================+++++++strategy fallback...........SELLS+++++++++===================================");
                } else  {
                    // minimum price someone is selling for; buy at that amount or the last bid price
                    MarketData marketData = minAskPriceMarketData(productMarketDataList);
                    ExchangeOrder exchangeOrder = new ExchangeOrder(order, marketData.getExchangeUrl());
                    System.out.println("+=====Buy Signal condition fulfilled"+order.getProduct()+" : "+order.getSide()+"change in price ="+marketData.getChangeInPrice()+"=======");

                    // good to buy
                    // to be experimented; buy as quick as possible
                    if(order.getPrice() < marketData.getASK_PRICE()) {
                        System.out.println("+=====Adjusting price "+marketData.getASK_PRICE()+" : "+order.getSide()+"change in price ="+marketData.getChangeInPrice()+"=======");

                        order.setPrice(marketData.getASK_PRICE());
                        argComment ="better asked price";
                    } else {
                        System.out.println("+=====Adjusting price last t price "+marketData.getLAST_TRADED_PRICE()+" : "+order.getSide()+"change in price ="+marketData.getChangeInPrice()+"=======");

                        order.setPrice(marketData.getLAST_TRADED_PRICE());
                        argComment = "did price adjusted to last bid price";
                    }
                    System.out.println("+=====Sending to exchange and report ========");

                    toExchange(exchangeOrder);
                    toReport(order.getId(),"exchanged",marketData.getExchangeUrl(),
                            "Signal condition : +" + signal +" met at patience "+ patience+" "+argComment+" "+comment);
                    String status = "completed";
                    saveOrUpdateQueueItem(patience, signal, exchangeOrder, status);
                    System.out.println("+==========================+++++++strategy fallback...........PURCHASE+++++++++===================================");
                }
            } else  {
                // checking pricing at market
                evaluateTradeOrder(order, patience, signal);
            }
        }

        // do use patience, signal and price adjustment
        if (order.getStrategy() == 2) {
            int patience = 20;
            double signal = 0.2;
            evaluateTradeOrder(order, patience, signal);
        }

        // do use higher ^patience, ^signal and ^price adjustment
        if (order.getStrategy() == 3) {
            int patience = 12;
            double signal = 0.3;
            List<MarketData> productMarketDataList = MarketDataList.getMarketDataListByTicker(order.getProduct());
            // checking pricing at market
            evaluateTradeOrder(order, patience, signal);
        }
        if (order.getStrategy() == 4) {
            int patience = 12;
            double signal = 0.5;
            evaluateTradeOrder(order, patience, signal);
        }

        // do use higher ^patience, ^signal and ^price adjustment
        if (order.getStrategy() == 5) {
            int patience = 12;
            double signal = 0.8;
            List<MarketData> productMarketDataList = MarketDataList.getMarketDataListByTicker(order.getProduct());
            // checking pricing at market
            evaluateTradeOrder(order, patience, signal);
        }

        if (order.getStrategy() == 6) {
            int patience = 12;
            double signal = 1;
            evaluateTradeOrder(order, patience, signal);
        }

        // do use higher ^patience, ^signal and ^price adjustment
        if (order.getStrategy() == 7) {
            int patience = 12;
            double signal = 2;
            List<MarketData> productMarketDataList = MarketDataList.getMarketDataListByTicker(order.getProduct());
            // checking pricing at market
            evaluateTradeOrder(order, patience, signal);
        }

        if (order.getStrategy() == 8) {
            int patience = 12;
            double signal = 3;
            evaluateTradeOrder(order, patience, signal);
        }
    }

    private void evaluateTradeOrder(Order order, int patience, double signal) {
        System.out.println("+=====Evaluating trade "+order.getProduct()+" : "+order.getSide()+" with Signal ="+signal+"===Patience="+patience+"=======");
        List<MarketData> productMarketDataList = MarketDataList.getMarketDataListByTicker(order.getProduct());

        if(patience < 0) {
            // when patience is exhausted evaluate trade with short term strategy
            int newStrategy = order.getStrategy() - 1 == 1? 10: order.getStrategy() - 1;
            order.setStrategy(newStrategy);
            System.out.println("+=====Patience reduce te 0 strategy set to 4 "+order.getProduct()+" : "+order.getSide()+"with Signal ="+signal+"===Patience="+patience+"=======");
            tradeOrder(order);
        } else if (order.getSide().equals("BUY")) {
            System.out.println("+=====Buying branch "+order.getProduct()+" : "+order.getSide()+" with Signal ="+signal+"===Patience="+patience+"=======");
            //if buying or selling
            evaluateBuyOrder(order, patience, signal, productMarketDataList);
        } else {
            System.out.println("+=====Selling branch "+order.getProduct()+" : "+order.getSide()+" with Signal ="+signal+"===Patience="+patience+"=======");
            evaluateSellOrder(order, patience, signal, productMarketDataList);
        }
    }

    private void evaluateSellOrder(Order order, int patience, double signal, List<MarketData> productMarketDataList) {
        // sell if price is falling
        // highest price someone is willing to buy
        MarketData marketData = maxBidPriceMarketData(productMarketDataList);
        ExchangeOrder exchangeOrder = new ExchangeOrder(order, marketData.getExchangeUrl());
        // reduce patience if they is a change in price
        if(patience !=0) patience -=1;
            String argComment ="";
        if (marketData.getChangeInPrice() <= -signal) {
            System.out.println("+=====Sell Signal condition fulfilled "+order.getProduct()+" : "+order.getSide()+ " change in price ="+marketData.getChangeInPrice()+"=======");
            // good to sell
            //modify price to cell at max price bid price or last traded price
            // to be experimented sell as quick as possible
            if(order.getPrice() <= marketData.getBID_PRICE()) {
                System.out.println("+=====Adjusting price "+marketData.getBID_PRICE()+" : "+order.getSide()+" change in price ="+marketData.getChangeInPrice()+"=======");
                order.setPrice(marketData.getBID_PRICE());
                argComment ="better bid price";
            } else {
                System.out.println("+=====Adjusting price last t price "+marketData.getLAST_TRADED_PRICE()+" : "+order.getSide()+"change in price ="+marketData.getChangeInPrice()+"=======");
                order.setPrice(marketData.getLAST_TRADED_PRICE());
                argComment = "ask price adjusted to last bid price";
            }
            System.out.println("+=====Sending to exchange and report ========");

            toExchange(exchangeOrder);
            toReport(order.getId(),"exchanged",marketData.getExchangeUrl(),
                    "Signal condition : -" + signal +" met at patience "+ patience+" "+argComment);
            String status = "completed";
            saveOrUpdateQueueItem(patience, signal, exchangeOrder, status);
            System.out.println("+==========================+++++++HUUURE...........EEEERRRR+++++++++===================================");
        } else {
            System.out.println("+=====Signal ***NOT*** condition fulfilled "+order.getProduct()+" : "+order.getSide()+"change in price ="+marketData.getChangeInPrice()+"=======");

            System.out.println("+=====Sending to report");
            toReport(order.getId(),"held","Best exchange : "+marketData.getExchangeUrl(),
                    "Signal condition : "+ signal +" not met at patience "+ patience);
            // save with status and
            // Queue order and wait for change in market data
            String status = "incomplete";
            saveOrUpdateQueueItem(patience, signal, exchangeOrder, status);
            System.out.println("+==========================-------------------------===================================");
        }
    }

    private void evaluateBuyOrder(Order order, int patience, double signal, List<MarketData> productMarketDataList) {
        // reduce patience if they is a change in price
        if(patience !=0) patience -=1;
        // we need to buy when price is increasing
        // minimum price someone is selling for
        MarketData marketData = minAskPriceMarketData(productMarketDataList);
        ExchangeOrder exchangeOrder = new ExchangeOrder(order, marketData.getExchangeUrl());
        String argComment ="";
        // thus product price is increasing
        if (marketData.getChangeInPrice() >= signal) {
            System.out.println("+=====Buy Signal condition fulfilled"+order.getProduct()+" : "+order.getSide()+"change in price ="+marketData.getChangeInPrice()+"=======");

            // good to buy
            // to be experimented; buy as quick as possible
            if(order.getPrice() <= marketData.getASK_PRICE()) {
                System.out.println("+=====Adjusting price "+marketData.getASK_PRICE()+" : "+order.getSide()+"change in price ="+marketData.getChangeInPrice()+"=======");

                order.setPrice(marketData.getASK_PRICE());
                argComment ="better asked price";
            } else {
                System.out.println("+=====Adjusting price last t price "+marketData.getLAST_TRADED_PRICE()+" : "+order.getSide()+"change in price ="+marketData.getChangeInPrice()+"=======");

                order.setPrice(marketData.getLAST_TRADED_PRICE());
                argComment = "did price adjusted to last bid price";
            }
            System.out.println("+=====Sending to exchange and report ========");

            toExchange(exchangeOrder);
            toReport(order.getId(),"exchanged",marketData.getExchangeUrl(),
                    "Signal condition : +" + signal +" met at patience "+ patience+" "+argComment);
            String status = "completed";
            saveOrUpdateQueueItem(patience, signal, exchangeOrder, status);
            System.out.println("+==========================+++++++HUUURE...........EEEERRRR+++++++++===================================");
        } else {
            System.out.println("+=====Signal ***NOT*** condition fulfilled"+order.getProduct()+" : "+order.getSide()+"change in price ="+marketData.getChangeInPrice()+"=======");

            System.out.println("+=====Sending to report ========");

            toReport(order.getId(),"held","Best exchange : "+marketData.getExchangeUrl(),
                    "Signal condition : "+ signal +" not met at patience "+ patience);
            // save with status and
            // Queue order and wait for change in market data
            String status = "incomplete";
            saveOrUpdateQueueItem(patience, signal, exchangeOrder, status);

            System.out.println("+==========================----------+:(---------------===================================");
        }
    }

    private void saveOrUpdateQueueItem(int patience, double signal, ExchangeOrder exchangeOrder, String status) {
        List<EngineQueueItem> dbEnqueuedItems = engineQueueItemRepository.findByOrderId(exchangeOrder.getOrder().getId());
        EngineQueueItem queueItem =null;
        if (dbEnqueuedItems.isEmpty()) {
            engineQueueItemRepository.save(new EngineQueueItem(exchangeOrder, patience, signal, status));
            System.out.println("+=========New queue item+++++++++===================================");
        } else {
            queueItem = dbEnqueuedItems.get(0);
            queueItem.setExchangeOrder(exchangeOrder);
            queueItem.setPatience(patience);
            queueItem.setSignal(signal);
            queueItem.setStatus(status);
            System.out.println("+=========Updated queue item+++++++++===================================");
        }
        if(status=="incomplete") {
            boolean x= EngineQueueRegister.getEngineQueueRegister().add(queueItem);
            System.out.println("+=====Queue Item saved and added to  Register; S -> "+x+" ========");
        }
    }

    private void toReport(long orderId, String status, String exchangeUrl, String comment) {
        System.out.println("+=====Reporting X -> ");
        // report activity
//        redisQueueService.sendReportOrderToQueue(
//                new TradeEngineActivity(status, exchangeUrl, comment, orderId));

        try {
            ResponseEntity<Void> result = restTemplate.postForEntity("http://localhost:8086/trade-engine-activity", new TradeEngineActivity(status, exchangeUrl,
                    comment, orderId), Void.class);
            System.out.println("$ Report status: -> "+ result.getStatusCode());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void toExchange(ExchangeOrder exchangeOrder) {
        System.out.println("+=====Exchanging X -> ");
        // send to Exchange
//        redisQueueService.sendOrderToQueue(exchangeOrder);
        try {
            ResponseEntity<Void> result = restTemplate.postForEntity("http://localhost:8087/trade-exchange", exchangeOrder, Void.class);
            System.out.println("$ Exchange status: -> "+ result.getStatusCode());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//
//        webClientBuilder.build()
//                .post().uri("http://localhost:8087/trade-exchange")
//                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
//                .body(Mono.just(exchangeOrder), ExchangeOrder.class)
//            .retrieve()
//                .bodyToMono(Void.class);

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
        System.out.println("+=====Product list X -> "+ productMarketDataList.toString());
        System.out.println("+=====Exchange with max (good to sell) Bid price is -> "+maxPriceMd.getExchangeUrl()
                +" max bid price is " + maxPriceMd.getBID_PRICE() +" change in P " +maxPriceMd.getChangeInPrice() );
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
        System.out.println("+=====Product list S -> "+ productMarketDataList.toString());
        System.out.println("+=====Exchange with min (good to buy) ask price is -> "+minPriceMd.getExchangeUrl()
                +" min ask price  is" + minPriceMd.getASK_PRICE() +" change in Price " +minPriceMd.getChangeInPrice() );
        return minPriceMd;
    }
}
