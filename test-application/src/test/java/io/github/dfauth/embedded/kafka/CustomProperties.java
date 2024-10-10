package io.github.dfauth.embedded.kafka;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "apicurio")
@Component
public class CustomProperties {

    private final Map<String, Object> registry = new HashMap<>();

    @Bean
    @Qualifier("serde.config")
    public Map<String, Object> serdeConfig() {
        return registry;
    }

}