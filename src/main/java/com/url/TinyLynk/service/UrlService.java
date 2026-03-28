package com.url.TinyLynk.service;

import com.url.TinyLynk.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UrlService {

    private final UrlRepository urlRepository;

    public String shortenUrl(String originalUrl) {
        // todo
        return null;
    }
}
