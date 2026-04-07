package com.url.TinyLynk.controller;

import com.url.TinyLynk.dto.ShortenRequestDto;
import com.url.TinyLynk.dto.ShortenResponseDto;
import com.url.TinyLynk.model.UrlMapping;
import com.url.TinyLynk.service.UrlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UrlController {

    private final UrlService urlService;

    @PostMapping("/shorten")
    public ResponseEntity<ShortenResponseDto> shortenUrl(@Valid @RequestBody ShortenRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(urlService.shortenUrl(request));
    }

    @GetMapping("/url/{shortCode}/stats")
    public ResponseEntity<UrlMapping> getStats(@PathVariable String shortCode) {
        return ResponseEntity.ok(urlService.getStats(shortCode));
    }

    @DeleteMapping("/url/{shortCode}")
    public ResponseEntity<Void> deactivateUrl(@PathVariable String shortCode) {
        urlService.deactivateUrl(shortCode);
        return ResponseEntity.noContent().build();
    }

}
