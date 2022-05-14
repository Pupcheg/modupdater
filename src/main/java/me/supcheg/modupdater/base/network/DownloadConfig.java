package me.supcheg.modupdater.base.network;

import me.supcheg.modupdater.base.mod.Mod;
import me.supcheg.modupdater.base.mod.ModType;
import me.supcheg.modupdater.base.mod.SmallModInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

public record DownloadConfig(@NotNull Mod getMod, @NotNull Collection<String> getMinecraftVersions,
                             @NotNull Path getDownloadFolder,
                             @NotNull ModType getModType) {

    public boolean isCorrect(@Nullable SmallModInfo info) {
        return info != null &&
                info.getSupportedModTypes().contains(getModType) &&
                info.getSupportedMinecraftVersions().stream().anyMatch(s -> getMinecraftVersions.stream().anyMatch(s::contains));
    }

    public boolean isCorrect(@Nullable String name) {
        return name != null && getModType.test(name) && getMinecraftVersions.stream().anyMatch(name::contains);
    }


    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final Collection<String> minecraftVersions;
        private Mod mod;
        private Path downloadFolder;
        private ModType modType;

        private Builder() {
            minecraftVersions = new HashSet<>();
        }

        private Builder(@NotNull Builder other) {
            this.minecraftVersions = other.minecraftVersions;
            this.mod = other.mod;
            this.downloadFolder = other.downloadFolder;
            this.modType = other.modType;
        }

        public @NotNull Builder mod(@NotNull Mod mod) {
            this.mod = mod;
            return this;
        }

        public @NotNull Builder minecraftVersions(@NotNull String minecraftVersion, @NotNull String... minecraftVersions) {
            this.minecraftVersions.add(minecraftVersion);
            if (minecraftVersions.length != 0) {
                this.minecraftVersions.addAll(Arrays.asList(minecraftVersions));
            }
            return this;
        }

        public @NotNull Builder minecraftVersions(@NotNull Collection<String> minecraftVersions) {
            this.minecraftVersions.addAll(minecraftVersions);
            return this;
        }

        public @NotNull Builder downloadFolder(@NotNull Path downloadFolder) {
            this.downloadFolder = downloadFolder;
            return this;
        }

        public @NotNull Builder modType(@NotNull ModType modType) {
            this.modType = modType;
            return this;
        }

        public @NotNull Builder copyWithMod(@NotNull Mod mod) {
            return new Builder(this).mod(mod);
        }

        public @NotNull DownloadConfig build() {
            if (minecraftVersions.isEmpty()) {
                throw new IllegalStateException("Minecraft versions can't be empty");
            }
            Objects.requireNonNull(mod);
            Objects.requireNonNull(downloadFolder);
            Objects.requireNonNull(modType);

            return new DownloadConfig(mod, minecraftVersions, downloadFolder, modType);
        }
    }
}