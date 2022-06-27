package me.supcheg.modupdater.common.concurrent;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;

public class IntermediateResultProcess<I, R> {

    @NotNull
    public static <I, R> IntermediateResultProcess<I, R> completed(R result) {
        return new IntermediateResultProcess<>(result);
    }

    @NotNull
    @Contract(value = " -> new", pure = true)
    public static <I, R> Builder<I, R> builder() {
        return new Builder<>();
    }

    private final CompletableFuture<R> completableFuture;
    private volatile I intermediateResult;

    private final ChangeConsumer<I> onChange;
    private final Function<Exception, R> exceptionallyResult;

    private IntermediateResultProcess(@NotNull R result) {
        onChange = null;
        exceptionallyResult = null;

        this.completableFuture = CompletableFuture.completedFuture(result);
    }


    private IntermediateResultProcess(@NotNull Builder<I, R> builder) {
        var function = builder.function;
        onChange = builder.onChange;
        exceptionallyResult = builder.exceptionallyResult;

        var accessor = new IntermediateResultAccessor();
        this.completableFuture = CompletableFuture.supplyAsync(() -> function.apply(accessor), builder.executor);
        if (builder.after != null) {
            this.completableFuture.thenAcceptAsync(builder.after);
        }
    }

    public @NotNull CompletableFuture<R> toCompletableFuture() {
        return completableFuture;
    }

    public I getIntermediateResult() {
        synchronized (this) {
            return intermediateResult;
        }
    }

    public R get() {
        try {
            return completableFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            if (exceptionallyResult == null) {
                throw new RuntimeException(e);
            } else {
                return exceptionallyResult.apply(e);
            }
        }
    }

    public void join() {
        completableFuture.join();
    }

    public class IntermediateResultAccessor {
        public void set(@Nullable I intermediateResult) {
            I old;
            synchronized (IntermediateResultProcess.this) {
                old = IntermediateResultProcess.this.intermediateResult;
                IntermediateResultProcess.this.intermediateResult = intermediateResult;
            }
            if (onChange != null) {
                onChange.accept(old, intermediateResult);
            }
        }
    }

    public static class Builder<I, R> {
        private Executor executor;
        private IntermediateResultAccessorFunction<I, R> function;
        private ChangeConsumer<I> onChange;
        private Consumer<R> after;
        private Function<Exception, R> exceptionallyResult;

        public @NotNull Builder<I, R> executor(@NotNull Executor executor) {
            this.executor = executor;
            return this;
        }

        public @NotNull Builder<I, R> function(@NotNull IntermediateResultAccessorFunction<I, R> function) {
            this.function = function;
            return this;
        }

        public @NotNull Builder<I, R> onChange(@Nullable ChangeConsumer<I> onChange) {
            this.onChange = onChange;
            return this;
        }

        public @NotNull Builder<I, R> after(@Nullable Consumer<R> after) {
            this.after = after;
            return this;
        }

        public @NotNull Builder<I, R> defaultResult(@Nullable Function<Exception, R> exceptionallyResult) {
            this.exceptionallyResult = exceptionallyResult;
            return this;
        }

        @NotNull
        @Contract(" -> new")
        public IntermediateResultProcess<I, R> run() {
            Objects.requireNonNull(executor);
            Objects.requireNonNull(function);
            return new IntermediateResultProcess<>(this);
        }
    }
}
