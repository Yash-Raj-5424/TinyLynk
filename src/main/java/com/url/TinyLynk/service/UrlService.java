package com.url.TinyLynk.service;

import com.url.TinyLynk.dto.ClickEvent;
import com.url.TinyLynk.dto.ShortenRequestDto;
import com.url.TinyLynk.dto.ShortenResponseDto;
import com.url.TinyLynk.exceptions.UrlExpiredException;
import com.url.TinyLynk.exceptions.UrlNotFoundException;
import com.url.TinyLynk.model.UrlMapping;
import com.url.TinyLynk.repository.UrlMappingRepository;
import com.url.TinyLynk.util.Base62Encoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrlService {

    private final UrlMappingRepository urlMappingRepository;
    private final Base62Encoder encoder;
    private final RedisTemplate<String, Object> redisTemplate;
    private final KafkaTemplate<String, ClickEvent> kafkaTemplate;

    private static final String CACHE_PREFIX = "url:";
    private static final long CACHE_TTL_SECONDS = 86400L;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.kafka.topic.click-events}")
    private String clickEventsTopic;

    public ShortenResponseDto shortenUrl(ShortenRequestDto request) {

        UrlMapping mapping = UrlMapping.builder()
                .originalUrl(request.getUrl())
                .expiresAt(request.getExpiresAt())
                .build();
        mapping = urlMappingRepository.save(mapping);

        String shortCode = encoder.encode(mapping.getId());
        mapping.setShortCode(shortCode);
        mapping = urlMappingRepository.save(mapping);

        log.info("Created Short URL: {} -> {}", shortCode, request.getUrl());

        return ShortenResponseDto.builder()
                .shortUrl(baseUrl + "/" + shortCode)
                .shortCode(shortCode)
                .expiresAt(mapping.getExpiresAt())
                .build();
    }

    public String resolveCode(String shortCode) {
        String cacheKey = CACHE_PREFIX + shortCode;

        UrlMapping cached = (UrlMapping) redisTemplate.opsForValue().get(cacheKey);

        UrlMapping mapping;
        if(cached != null) {
            if(cached.getExpiresAt() != null && OffsetDateTime.now().isAfter(cached.getExpiresAt())) {
                redisTemplate.delete(cacheKey);
                throw new UrlExpiredException(shortCode);
            }
            log.info("Cache hit for short code: {}", shortCode);
            mapping = cached;
        } else {
            log.info("Cache miss for short code: {}", shortCode);

            mapping = urlMappingRepository.findByShortCodeAndActiveTrue(shortCode)
                    .orElseThrow(() -> new UrlNotFoundException(shortCode));

            if(mapping.getExpiresAt() != null && OffsetDateTime.now().isAfter(mapping.getExpiresAt())) {
                mapping.setActive(false);
                urlMappingRepository.save(mapping);
                throw new UrlExpiredException(shortCode);
            }

            redisTemplate.opsForValue().set(cacheKey, mapping, CACHE_TTL_SECONDS, TimeUnit.SECONDS);
        }

        recordClick(shortCode);

        return mapping.getOriginalUrl();
    }

    private void recordClick(String shortCode) {
        try {
            kafkaTemplate.send(clickEventsTopic, shortCode, ClickEvent.builder()
                    .shortCode(shortCode)
                    .clickedAt(OffsetDateTime.now())
                    .build());
        } catch (Exception e) {
            log.warn("Failed to publish click event for short code {}: {}", shortCode, e.getMessage());
        }
    }

    public UrlMapping getStats(String shortCode) {
        return urlMappingRepository.findByShortCodeAndActiveTrue(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(shortCode));
    }

    public void deactivateUrl(String shortCode) {
        UrlMapping mapping = urlMappingRepository.findByShortCodeAndActiveTrue(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(shortCode));

        mapping.setActive(false);
        urlMappingRepository.save(mapping);
        redisTemplate.delete(CACHE_PREFIX + shortCode);

        log.info("Deactivated short code: {}", shortCode);
    }
}
