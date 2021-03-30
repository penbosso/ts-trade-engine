package com.example.tstradeengine.service;

import com.example.tstradeengine.model.Exchange;
import com.example.tstradeengine.model.ExchangeOrder;
import com.example.tstradeengine.model.Order;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class OrderMessageSubscriber implements MessageListener {
    private static int count =0;
    @Autowired
    private RedisQueueService redisQueueService;

    @Autowired
    private TradeEngineService tradeEngineService;

    ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public void onMessage(Message message, byte[] bytes) {
        count = count +1;
        try {
            Order order = objectMapper.readValue(message.toString(), Order.class);
           // give the order to trade engine
            if(order.isValid()) tradeEngineService.tradeOrder(order);

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Message -> "+count+" received: "+ message);
    }
}
