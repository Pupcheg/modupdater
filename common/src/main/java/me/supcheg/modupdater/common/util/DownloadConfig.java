package me.supcheg.modupdater.common.util;

import me.supcheg.modupdater.common.config.Config;
import me.supcheg.modupdater.common.mod.Mod;
import me.supcheg.modupdater.common.mod.ModType;
import me.supcheg.modupdater.common.mod.SupportInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Objects;
import java.util.StringJoiner;

public class DownloadConfig {

    private final Mod mod;
    private final String minecraftVersion;
    private final Path downloadFolder;
    private final ModType modType;


    private DownloadConfig(@NotNull Builder builder) {
        this.mod = builder.mod;
        this.minecraftVersion = builder.minecraftVersion;
        this.downloadFolder = builder.downloadFolder;
        this.modType = builder.modType;
    }

    public @NotNull Mod getMod() {
        return mod;
    }

    public @NotNull ModType getModType() {
        return modType;
    }

    public @NotNull Path getDownloadFolder() {
        return downloadFolder;
    }

    public @NotNull String getMinecraftVersion() {
        return minecraftVersion;
    }

    public boolean isCorrect(@Nullable SupportInfo info) {
        return info != null &&
                info.getModTypes().contains(modType) &&
                info.getMinecraftVersions().stream().anyMatch(s -> s.contains(minecraftVersion));
    }

    public boolean isCorrect(@Nullable String name) {
        return name != null && modType.notContainsOpposite(name) && name.contains(minecraftVersion);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", "DownloadConfig{", "}")
                .add("mod=" + mod)
                .add("minecraftVersion='" + minecraftVersion + "'")
                .add("downloadFolder=" + downloadFolder)
                .add("modType=" + modType)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DownloadConfig that = (DownloadConfig) o;
        return Objects.equals(mod, that.mod)
                && Objects.equals(minecraftVersion, that.minecraftVersion)
                && Objects.equals(downloadFolder, that.downloadFolder)
                && modType == that.modType;
    }

    @Override
    public int hashCode() {
        int result = mod != null ? mod.hashCode() : 0;
        result = 31 * result + (minecraftVersion != null ? minecraftVersion.hashCode() : 0);
        result = 31 * result + (downloadFolder != null ? downloadFolder.hashCode() : 0);
        result = 31 * result + (modType != null ? modType.hashCode() : 0);
        return result;
    }

    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static class Builder {
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

        public @NotNull Builder defaultsFrom(@NotNull Config config) {
            return minecraftVersion(config.getMinecraftVersion())
                    .downloadFolder(config.getDownloadFolder())
                    .modType(config.getModsType());
        }

        public @NotNull DownloadConfig buildCopyWithMod(@NotNull Mod mod) {
            return new Builder(this).mod(mod).build();
        }

        public @NotNull Builder copyWithMod(@NotNull Mod mod) {
            return new Builder(this).mod(mod);
        }

        public @NotNull DownloadConfig build() {
            Objects.requireNonNull(minecraftVersion);
            Objects.requireNonNull(mod);
            Objects.requireNonNull(downloadFolder);
            Objects.requireNonNull(modType);

            return new DownloadConfig(this);
        }
    }
}