package io.github.dfauth.kafka.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.Lifecycle;
import org.springframework.kafka.annotation.KafkaListener;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static io.github.dfauth.embedded.kafka.util.CompletableFutureUtil.onException;
import static io.github.dfauth.embedded.kafka.util.CompletableFutureUtil.peek;

@Slf4j
public class KafkaCache<T,R,K,V> implements Lifecycle {

    private Cache<K, V> cache;
    private Function<K, CompletableFuture<V>> onCacheMiss;
    private Function<T,K> keyMapper;
    private Function<R,V> valueMapper;
    private UnaryOperator<CacheBuilder<Object, Object>> customizer = UnaryOperator.identity();

    public KafkaCache(Function<K, CompletableFuture<V>> onCacheMiss, Function<T, K> keyMapper, Function<R, V> valueMapper) {
        this.onCacheMiss = onCacheMiss;
        this.keyMapper = keyMapper;
        this.valueMapper = valueMapper;
    }

    public void  customize(Consumer<CacheBuilder<Object,Object>> customizer) {
        this.customizer = peek(customizer);
    }

    @Override
    public void start() {
        cache = customizer.apply(CacheBuilder.newBuilder())
                .build();
    }

    @Override
    public void stop() {
        this.cache.cleanUp();
        this.cache = null;
    }

    @Override
    public boolean isRunning() {
        return cache != null;
    }

    @KafkaListener()
    public void onMessage(ConsumerRecord<T,R> consumerRecord) {
        cache.put(
            keyMapper.apply(consumerRecord.key()),
            valueMapper.apply(consumerRecord.value())
        );
    }

    public Optional<V> getOptional(K k) {
        return Optional.ofNullable(cache.getIfPresent(k));
    }

    public void getInCallback(K k, Consumer<V> callback) {
        get(k).thenAccept(callback);
    }

    public CompletableFuture<V> get(K k) {
        return getOptional(k).map(CompletableFuture::completedFuture).orElseGet(() ->
                onException(onCacheMiss.apply(k), e -> cache.invalidate(k)).thenApply(peek(v -> cache.put(k,v))));
    }
}
