package com.bookstore.book_service.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis Cache Configuration for Book Service.
 *
 * Defines per-cache TTL (Time-To-Live) settings based on data volatility:
 * - Book lookups: 30 min (data changes on updates)
 * - Categories: 60 min (rarely change)
 * - Homepage widgets (featured/latest): 10-15 min (moderate freshness)
 * - Dashboard counts: 5 min (aggregates, can be slightly stale)
 */
@Configuration
public class CacheConfig {

    // ── Cache Name Constants ──
    public static final String CACHE_BOOKS = "books";
    public static final String CACHE_BOOKS_BY_ISBN = "books-by-isbn";
    public static final String CACHE_CATEGORIES = "categories";
    public static final String CACHE_CATEGORIES_BY_SLUG = "categories-by-slug";
    public static final String CACHE_CATEGORY_HIERARCHY = "category-hierarchy";
    public static final String CACHE_FEATURED_BOOKS = "featured-books";
    public static final String CACHE_LATEST_BOOKS = "latest-books";
    public static final String CACHE_BOOK_COUNTS = "book-counts";

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {

        // Default cache configuration — 30 minute TTL
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer()))
                .disableCachingNullValues();

        // Per-cache TTL overrides
        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();

        // Book lookups — 30 min
        cacheConfigs.put(CACHE_BOOKS, defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigs.put(CACHE_BOOKS_BY_ISBN, defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // Category lookups — 60 min (categories rarely change)
        cacheConfigs.put(CACHE_CATEGORIES, defaultConfig.entryTtl(Duration.ofMinutes(60)));
        cacheConfigs.put(CACHE_CATEGORIES_BY_SLUG, defaultConfig.entryTtl(Duration.ofMinutes(60)));
        cacheConfigs.put(CACHE_CATEGORY_HIERARCHY, defaultConfig.entryTtl(Duration.ofMinutes(60)));

        // Homepage widgets — shorter TTL for freshness
        cacheConfigs.put(CACHE_FEATURED_BOOKS, defaultConfig.entryTtl(Duration.ofMinutes(15)));
        cacheConfigs.put(CACHE_LATEST_BOOKS, defaultConfig.entryTtl(Duration.ofMinutes(10)));

        // Dashboard counts — 5 min (acceptable staleness for aggregates)
        cacheConfigs.put(CACHE_BOOK_COUNTS, defaultConfig.entryTtl(Duration.ofMinutes(5)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .transactionAware()
                .build();
    }

    /**
     * JSON serializer for Redis values.
     * Uses Jackson with type info so deserialization works correctly for polymorphic types.
     */
    private GenericJackson2JsonRedisSerializer jsonSerializer() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        return new GenericJackson2JsonRedisSerializer(mapper);
    }
}
