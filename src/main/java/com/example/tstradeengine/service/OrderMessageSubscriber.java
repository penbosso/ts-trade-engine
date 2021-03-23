package com.example.tstradeengine.service;

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
    private MessageGateway messageGateway;
    ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public void onMessage(Message message, byte[] bytes) {
        count = count +1;
        try {
            Order order = objectMapper.readValue(message.toString(), Order.class);

            //Todo to do some algo and send order to exchange
            //When to hold
            //when to split
            // report trade activity
            messageGateway.sendMessage(order);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Message -> "+count+" received: "+ message);
    }
}
