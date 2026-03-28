package com.url.TinyLynk.repository;

import com.url.TinyLynk.model.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UrlRepository extends JpaRepository<Long, UrlMapping> {
    Optional<UrlMapping> findByShortCode(String shortCode);
}
