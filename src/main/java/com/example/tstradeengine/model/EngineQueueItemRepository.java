package com.example.tstradeengine.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EngineQueueItemRepository extends JpaRepository<EngineQueueItem, Long> {
    List<EngineQueueItem> findByStatus(String status);
    List<EngineQueueItem> findByOrderId(long orderId);
}
