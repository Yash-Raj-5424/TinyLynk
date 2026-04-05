package com.url.TinyLynk.service;

import com.url.TinyLynk.model.UrlMapping;
import com.url.TinyLynk.repository.UrlMappingRepository;
import com.url.TinyLynk.util.Base62Encoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrlService {

    private final UrlMappingRepository urlMappingRepository;
    private final Base62Encoder encoder;

    @Value("${app.base-url}")
    private String baseUrl;

    public String shortenUrl(String originalUrl) {

        UrlMapping mapping = UrlMapping.builder()
                .originalUrl(originalUrl)
                .build();
        mapping = urlMappingRepository.save(mapping);

        String shortCode = encoder.encode(mapping.getId());
        mapping.setShortCode(shortCode);
        urlMappingRepository.save(mapping);

        log.info("Created Short URL: {} -> {}", shortCode, originalUrl);

        return baseUrl + "/" + shortCode;
    }

    public String resolveCode(String shortCode) {
        UrlMapping mapping = urlMappingRepository.findByShortCodeAndActiveTrue(shortCode)
                .orElseThrow(() -> new RuntimeException("Short code not found: " + shortCode));
        return mapping.getOriginalUrl();
    }
}
