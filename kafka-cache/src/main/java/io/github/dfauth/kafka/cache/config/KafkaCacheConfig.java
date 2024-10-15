package io.github.dfauth.kafka.cache.config;

import io.github.dfauth.kafka.cache.KafkaCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class KafkaCacheConfig {

    @Bean
    public <K,V> KafkaCache<K,V> kafkaCache() {
        return new KafkaCache<>();
    }
}
