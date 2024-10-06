package io.github.dfauth.embedded.kafka.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.EmbeddedKafkaZKBroker;

import java.util.Map;
import java.util.Properties;

@Slf4j
@Configuration
public class KafkaConfig {

    @Value("${io.github.dfauth.embedded.kafka.listener:PLAINTEXT://0.0.0.0:9092}")
    private String listeners;
    @Value("${io.github.dfauth.embedded.kafka.advertised.listener:PLAINTEXT://localhost:9092}")
    private String advertisedListeners;
    @Value("${io.github.dfauth.embedded.kafka.broker.count:1}")
    private int brokers;
    @Value("${io.github.dfauth.embedded.kafka.controlled.shutdown:true}")
    private boolean controlledShutdown;
    @Value("${io.github.dfauth.embedded.kafka.partition.count:1}")
    private int partitions;
    @Value("${io.github.dfauth.embedded.kafka.topics:TOPIC}")
    private String[] topics;
    @Bean
    @Qualifier("brokerProperties")
    public Properties brokerProperties() {
        Properties props = new Properties();
        props.setProperty("listeners",listeners);
        props.setProperty("advertised.listeners",advertisedListeners);
        return props;
    }

    @Bean
    public EmbeddedKafkaBroker kafkaBroker(@Qualifier("brokerProperties") Map<String, String> brokerProperties) {
        try {
            EmbeddedKafkaZKBroker broker = new EmbeddedKafkaZKBroker(brokers, controlledShutdown, partitions, topics);
            broker.brokerProperties(brokerProperties);
            return broker;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

}
