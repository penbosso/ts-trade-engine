package com.example.tstradeengine.service;

import com.example.tstradeengine.model.ExchangeOrder;
import com.example.tstradeengine.model.TradeEngineActivity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisPool;

@Service
public class RedisQueueService {
    @Autowired
    private JedisPool jedisPool;


    public void sendOrderToQueue(ExchangeOrder exchangeOrder) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String exchangedOrderStr = objectMapper.writeValueAsString(exchangeOrder);
            jedisPool.getResource().rpush("exchange-trade", exchangedOrderStr);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public void sendReportOrderToQueue(TradeEngineActivity tradeEngineActivity) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String exchangedOrderStr = objectMapper.writeValueAsString(tradeEngineActivity);
            jedisPool.getResource().rpush("trade-activity", exchangedOrderStr);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
