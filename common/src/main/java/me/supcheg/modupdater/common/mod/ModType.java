package me.supcheg.modupdater.common.mod;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.EnumSet;
import java.util.function.Predicate;


public enum ModType {

    FABRIC(s -> !s.contains(Names.FORGE)),
    FORGE(s -> !s.contains(Names.FABRIC) && !s.contains(Names.QUILT));


    private final Predicate<String> predicate;

    ModType(@NotNull Predicate<String> predicate) {
        this.predicate = predicate;
    }

    @NotNull
    public String getName() {
        return name().toLowerCase();
    }

    public boolean notContainsOpposite(@NotNull String s) {
        return this.predicate.test(s.toUpperCase());
    }

    public static @NotNull EnumSet<ModType> fromStringCollection(@NotNull Collection<String> collection) {
        EnumSet<ModType> set = EnumSet.noneOf(ModType.class);

        for (String s : collection) {
            String lowerCase = s.toLowerCase();

            if (lowerCase.contains(Names.FORGE)) {
                set.add(FORGE);
            }
            if (lowerCase.contains(Names.FABRIC) || lowerCase.contains(Names.QUILT)) {
                set.add(ModType.FABRIC);
            }
        }

        return set;
    }

    private static class Names {
        private static final String FORGE = "forge";
        private static final String FABRIC = "fabric";
        private static final String QUILT = "quilt";
    }

}
