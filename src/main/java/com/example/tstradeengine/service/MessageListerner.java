package com.example.tstradeengine.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.redis.outbound.RedisQueueOutboundChannelAdapter;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Component
public class MessageListerner {
    @Autowired
    private RedisConnectionFactory redisConnectionFactory;
    @ServiceActivator(inputChannel = "tradeInputChannel", outputChannel = "senderChannel")
    public Message<?> receiveFromService(Message<?> message) {
        return message;
    }

    @ServiceActivator(inputChannel = "senderChannel")
    public void sendMessageToQueue(Message<?> message) {
        RedisQueueOutboundChannelAdapter adapter = new RedisQueueOutboundChannelAdapter("Redis-Queue", redisConnectionFactory);
        adapter.handleMessage(message);
    }
}
