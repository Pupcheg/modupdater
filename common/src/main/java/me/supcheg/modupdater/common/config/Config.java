package me.supcheg.modupdater.common.config;

import me.supcheg.modupdater.common.mod.Mod;
import me.supcheg.modupdater.common.mod.ModType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public interface Config {

    @NotNull
    Path getDownloadFolder();

    @NotNull
    Path getModsFolder();

    @NotNull
    String getMinecraftVersion();

    @NotNull
    ModType getModsType();

    @Nullable
    String get(@NotNull String address);

    void set(@NotNull String address, @NotNull String value);

    void setCustomUrl(@NotNull Mod mod, @NotNull String value);

    @Nullable
    String getCustomUrl(@NotNull Mod mod);
}

