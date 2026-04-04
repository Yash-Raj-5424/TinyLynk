package com.url.TinyLynk.repository;

import com.url.TinyLynk.model.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UrlMappingRepository extends JpaRepository<UrlMapping, Long> {

    Optional<UrlMapping> findByShortCodeAndActiveTrue(String shortCode);
    boolean existsByShortCode(String shortCode);

}
