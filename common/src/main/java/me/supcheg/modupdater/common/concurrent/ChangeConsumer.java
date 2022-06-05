package me.supcheg.modupdater.common.concurrent;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

@FunctionalInterface
public interface ChangeConsumer<T> extends BiConsumer<T, T> {

    @NotNull
    @Contract(pure = true)
    static <T> ChangeConsumer<T> onlyOldState(@NotNull Consumer<T> consumer) {
        return (oldState, newState) -> consumer.accept(oldState);
    }

    @NotNull
    @Contract(pure = true)
    static <T> ChangeConsumer<T> onlyNewState(@NotNull Consumer<T> consumer) {
        return (oldState, newState) -> consumer.accept(newState);
    }

    @Override
    void accept(@Nullable T oldState, @Nullable T newState);
}
