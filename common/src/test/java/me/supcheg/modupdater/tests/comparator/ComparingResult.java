package me.supcheg.modupdater.tests.comparator;

import org.jetbrains.annotations.NotNull;

public enum ComparingResult {
    UPPER, EQUALS, LOWER;

    public @NotNull ComparingResult reverse() {
        return switch (this) {
            case LOWER -> UPPER;
            case EQUALS -> EQUALS;
            case UPPER -> LOWER;
        };
    }

    public static @NotNull ComparingResult fromInt(int i) {
        if (i < 0) {
            return LOWER;
        } else if (i > 0) {
            return UPPER;
        } else {
            return EQUALS;
        }
    }
}
