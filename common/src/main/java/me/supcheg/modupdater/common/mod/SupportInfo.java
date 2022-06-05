package me.supcheg.modupdater.common.mod;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

public class SupportInfo {

    private final Set<String> minecraftVersions;
    private final Set<ModType> modTypes;

    public SupportInfo(@NotNull Collection<String> minecraftVersions,
                       @NotNull Collection<ModType> modTypes) {
        this.minecraftVersions = Set.copyOf(minecraftVersions);
        this.modTypes = Set.copyOf(modTypes);
    }

    public @NotNull Set<String> getMinecraftVersions() {
        return minecraftVersions;
    }

    public @NotNull Set<ModType> getModTypes() {
        return modTypes;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", "SupportInfo{", "}")
                .add("minecraftVersions=" + minecraftVersions)
                .add("modTypes=" + modTypes)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SupportInfo that = (SupportInfo) o;
        return Objects.equals(minecraftVersions, that.minecraftVersions) && Objects.equals(modTypes, that.modTypes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(minecraftVersions, modTypes);
    }
}
