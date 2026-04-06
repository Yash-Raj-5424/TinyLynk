package com.url.TinyLynk.service;

import com.url.TinyLynk.dto.ShortenRequestDto;
import com.url.TinyLynk.exceptions.UrlExpiredException;
import com.url.TinyLynk.exceptions.UrlNotFoundException;
import com.url.TinyLynk.model.UrlMapping;
import com.url.TinyLynk.repository.UrlMappingRepository;
import com.url.TinyLynk.util.Base62Encoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
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

    private static final String CACHE_PREFIX = "url:";
    private static final long CACHE_TTL_SECONDS = 86400L;

    @Value("${app.base-url}")
    private String baseUrl;

    public String shortenUrl(ShortenRequestDto request) {

        UrlMapping mapping = UrlMapping.builder()
                .originalUrl(request.getUrl())
                .expiresAt(request.getExpiresAt())
                .build();
        mapping = urlMappingRepository.save(mapping);

        String shortCode = encoder.encode(mapping.getId());
        mapping.setShortCode(shortCode);
        urlMappingRepository.save(mapping);

        log.info("Created Short URL: {} -> {}", shortCode, request.getUrl());

        return baseUrl + "/" + shortCode;
    }

    public String resolveCode(String shortCode) {
        String cacheKey = CACHE_PREFIX + shortCode;

        UrlMapping cached = (UrlMapping) redisTemplate.opsForValue().get(cacheKey);

        if(cached != null) {
            if(cached.getExpiresAt() != null && OffsetDateTime.now().isAfter(cached.getExpiresAt())) {
                redisTemplate.delete(cacheKey);
                throw new UrlExpiredException("Short code has expired: " + shortCode);
            }
            log.info("Cache hit for short code: {}", shortCode);
            return cached.getOriginalUrl();
        }

        log.info("Cache miss for short code: {}", shortCode);

        UrlMapping mapping = urlMappingRepository.findByShortCodeAndActiveTrue(shortCode)
                .orElseThrow(() -> new UrlNotFoundException("Short code not found: " + shortCode));

        if(mapping.getExpiresAt() != null && OffsetDateTime.now().isAfter(mapping.getExpiresAt())) {
            mapping.setActive(false);
            urlMappingRepository.save(mapping);
            throw new UrlExpiredException("Short code has expired: " + shortCode);
        }

        redisTemplate.opsForValue().set(cacheKey, mapping, CACHE_TTL_SECONDS, TimeUnit.SECONDS);

        return mapping.getOriginalUrl();
    }
}
