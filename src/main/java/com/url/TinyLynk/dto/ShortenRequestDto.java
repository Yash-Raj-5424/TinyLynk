package com.url.TinyLynk.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShortenRequestDto {

    private String url;
    private OffsetDateTime expiresAt;
}
