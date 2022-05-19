package me.supcheg.modupdater.base.network;

import me.supcheg.modupdater.base.mod.Mod;
import me.supcheg.modupdater.base.mod.ModType;
import me.supcheg.modupdater.base.mod.SmallModInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Objects;

public record DownloadConfig(@NotNull Mod getMod, @NotNull String getMinecraftVersion,
                             @NotNull Path getDownloadFolder,
                             @NotNull ModType getModType) {

    public boolean isCorrect(@Nullable SmallModInfo info) {
        return info != null &&
                info.getSupportedModTypes().contains(getModType) &&
                info.getSupportedMinecraftVersions().stream().anyMatch(s -> s.contains(getMinecraftVersion));
    }

    public boolean isCorrect(@Nullable String name) {
        return name != null && getModType.test(name) && name.contains(getMinecraftVersion);
    }


    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String minecraftVersion;
        private Mod mod;
        private Path downloadFolder;
        private ModType modType;

        private Builder() {
        }

        private Builder(@NotNull Builder other) {
            this.minecraftVersion = other.minecraftVersion;
            this.mod = other.mod;
            this.downloadFolder = other.downloadFolder;
            this.modType = other.modType;
        }

        public @NotNull Builder mod(@NotNull Mod mod) {
            this.mod = mod;
            return this;
        }

        public @NotNull Builder minecraftVersion(@NotNull String minecraftVersion) {
            this.minecraftVersion = minecraftVersion;
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
            Objects.requireNonNull(minecraftVersion);
            Objects.requireNonNull(mod);
            Objects.requireNonNull(downloadFolder);
            Objects.requireNonNull(modType);

            return new DownloadConfig(mod, minecraftVersion, downloadFolder, modType);
        }
    }
}