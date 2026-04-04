package com.url.TinyLynk.util;

import org.springframework.stereotype.Component;

@Component
public class Base62Encoder {

    private static final String CHARSET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int BASE = CHARSET.length();

    public String encode(long id) {
        StringBuilder shortCode = new StringBuilder();
        while (id > 0) {
            shortCode.append(CHARSET.charAt((int)(id % BASE)));
            id /= BASE;
        }
        return shortCode.reverse().toString();
    }

}
