package io.github.dfauth.embedded.kafka.util;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.function.Function.identity;

public class CompletableFutureUtil {

    public static <T,R> CompletableFuture<R> onException(CompletableFuture<T> fut, Function<Throwable, R> exceptionHandler, Function<T,R> handler) {
        return fut.handle((t, e) -> Optional.ofNullable(e).map(exceptionHandler).orElseGet(() -> handler.apply(t)));
    }

    public static <T> void onException(CompletableFuture<T> fut, Consumer<Throwable> exceptionHandler) {
        onException(fut, e -> {
            exceptionHandler.accept(e);
            return null;
        }, identity());
    }
}
