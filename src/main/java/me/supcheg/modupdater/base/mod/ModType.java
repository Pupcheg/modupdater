package me.supcheg.modupdater.base.mod;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.EnumSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;


public enum ModType implements Predicate<String> {

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

    @Override
    public boolean test(@NotNull String s) {
        return this.predicate.test(s.toLowerCase());
    }

    public static @NotNull EnumSet<ModType> fromStringCollection(@NotNull Collection<String> collection) {
        var lowerCase = collection.stream().map(String::toLowerCase).collect(Collectors.toSet());
        var set = EnumSet.noneOf(ModType.class);

        if (lowerCase.contains(Names.FORGE)) {
            set.add(FORGE);
        }
        if (lowerCase.contains(Names.FABRIC) || lowerCase.contains(Names.QUILT)) {
            set.add(ModType.FABRIC);
        }

        return set;
    }

    private static final class Names {
        private static final String FORGE = "forge";
        private static final String FABRIC = "fabric";
        private static final String QUILT = "quilt";
    }

}
