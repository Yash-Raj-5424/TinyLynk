package com.url.TinyLynk.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UrlEncoder {

    private static final String BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    public String encode(long id) {
        StringBuilder shortCode = new StringBuilder();
        while (id > 0) {
            int remainder = (int) (id % 62);
            shortCode.append(BASE62.charAt(remainder));
            id /= 62;
        }
        return shortCode.reverse().toString();
    }
}
