package com.example.tstradeengine.controller;

import com.example.tstradeengine.model.EngineQueueItem;
import com.example.tstradeengine.model.EngineQueueItemRepository;
import com.example.tstradeengine.model.MarketDataList;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class EngineQueueController {
    EngineQueueItemRepository engineQueueItemRepository;

    @GetMapping("v0/api/engine-queue")
    public List<EngineQueueItem> getEngineQueue() {
        return engineQueueItemRepository.findAll();
    }

    @PostMapping("v0/api/cancel-queued-order/{orderId}")
    public ResponseEntity<Object> removeQueueItem(@PathVariable(name = "orderId") long orderId ) {
        EngineQueueItem eq=  engineQueueItemRepository.findByOrderId(orderId).get(0);
        MarketDataList.getMarketDataList().remove(eq);
        eq.setStatus("cancelled");
        engineQueueItemRepository.save(eq);
        return ResponseEntity.ok().build();
    }
}
