package com.example.tstradeengine.service;

import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;

@MessagingGateway
public interface MessageGateway {
    @Gateway(requestChannel = "tradeChannel")
    public <S> void sendMessage(S request) ;

}
