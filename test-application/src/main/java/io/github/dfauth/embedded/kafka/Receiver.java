package io.github.dfauth.embedded.kafka;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.ConsumerSeekAware;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@Getter
public class Receiver<K,V> implements ConsumerSeekAware {

    private final CompletableFuture<V> f = new CompletableFuture<>();

    @KafkaListener(topics = {"test"})
    public void inMessage(ConsumerRecord<K,V> consumerRecord) {
        f.complete(consumerRecord.value());
    }

    @Override
    public void onPartitionsAssigned(Map<TopicPartition, Long> assignments, ConsumerSeekCallback callback) {
        assignments.entrySet().stream()
                .forEach(e -> callback
                        .seekToBeginning(e.getKey().topic(), e.getKey().partition()));
    }
}
