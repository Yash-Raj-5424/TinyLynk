package com.url.TinyLynk.controller;

import com.url.TinyLynk.dto.ShortenRequestDto;
import com.url.TinyLynk.service.UrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UrlController {

    private final UrlService urlService;

    @PostMapping("/shorten")
    public ResponseEntity<String> shortenUrl(@RequestBody ShortenRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(urlService.shortenUrl(request));
    }


}
