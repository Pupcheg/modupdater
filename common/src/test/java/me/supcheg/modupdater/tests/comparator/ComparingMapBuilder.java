package me.supcheg.modupdater.tests.comparator;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ComparingMapBuilder {

    public static @NotNull ComparingMapBuilder create() {
        return new ComparingMapBuilder();
    }

    private final ImmutableMap.Builder<Pair<String, String>, ComparingResult> immutableMapBuilder;

    private ComparingMapBuilder() {
        this.immutableMapBuilder = ImmutableMap.builder();
    }

    public @NotNull ComparingMapBuilder put(@NotNull String version1, @NotNull String version2, @NotNull ComparingResult expectedResult) {
        this.immutableMapBuilder.put(Pair.of(version1, version2), expectedResult);
        return this;
    }

    public @NotNull ComparingMapBuilder artificiallyIncrease() {
        build().forEach((pair, result) -> {
            if (result != ComparingResult.EQUALS) {
                put(pair.getRight(), pair.getLeft(), result.reverse());
            }
        });
        return this;
    }

    public @NotNull Map<Pair<String, String>, ComparingResult> build() {
        return this.immutableMapBuilder.build();
    }
}
