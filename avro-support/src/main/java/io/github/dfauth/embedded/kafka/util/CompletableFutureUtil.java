package io.github.dfauth.embedded.kafka.util;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static java.util.function.Function.identity;

@Slf4j
public class CompletableFutureUtil {

    public static <T> Mono<T> toMono(CompletableFuture<T> fut) {
        return toMono(fut, peekThrowable(e -> log.error(e.getMessage(), e)), identity());
    }

    public static <T,R> Mono<R> toMono(CompletableFuture<T> fut, Function<Throwable, R> exceptionHandler, Function<T,R> handler) {
        return Mono.fromFuture(fut).map(handler).onErrorResume(t -> Mono.just(exceptionHandler.apply(t)));
    }

    public static <T,R> CompletableFuture<R> onException(CompletableFuture<T> fut, Function<Throwable, R> exceptionHandler, Function<T,R> handler) {
        return fut.handle((t, e) -> Optional.ofNullable(e).map(exceptionHandler).orElseGet(() -> handler.apply(t)));
    }

    public static <T> CompletableFuture<T> logOnException(CompletableFuture<T> fut) {
        return onException(fut, peekThrowable(e -> log.error(e.getMessage(), e)), identity());
    }

    public static <T> Function<Throwable,T> peekThrowable(Consumer<Throwable> consumer) {
        return t -> {
            consumer.accept(t);
            return null;
        };
    }

    public static <T> UnaryOperator<T> peek(Consumer<T> consumer) {
        return t -> {
            consumer.accept(t);
            return t;
        };
    }

    public static <T> void onException(CompletableFuture<T> fut, Consumer<Throwable> exceptionHandler) {
        onException(fut, peekThrowable(exceptionHandler), identity());
    }
}
