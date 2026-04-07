package com.url.TinyLynk.consumer;

import com.url.TinyLynk.dto.ClickEvent;
import com.url.TinyLynk.repository.UrlMappingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClickEventConsumer {

    private final UrlMappingRepository urlMappingRepository;

    @KafkaListener(topics = "${app.kafka.topic.click-events}", groupId = "click-event-consumers")
    public void consume(ClickEvent event) {
        log.info("Received Click Event: {}", event.getShortCode());
        urlMappingRepository.incrementClickCount(event.getShortCode());
    }
}
