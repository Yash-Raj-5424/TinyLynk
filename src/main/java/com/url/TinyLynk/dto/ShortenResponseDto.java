package com.url.TinyLynk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShortenResponseDto {

    private String shortUrl;
    private String shortCode;
    private OffsetDateTime expiresAt;
}
