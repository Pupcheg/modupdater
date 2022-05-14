package me.supcheg.modupdater.base.config;

import me.supcheg.modupdater.base.mod.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Config {

    @Nullable
    String get(@NotNull String address);

    void set(@NotNull String address, @NotNull String value);

    void setCustomUrl(@NotNull Mod mod, @NotNull String value);

    @Nullable
    String getCustomUrl(@NotNull Mod mod);

    void setSpecificData(@NotNull Mod mod, @NotNull String value);

    @Nullable
    String getSpecificData(@NotNull Mod mod);
}

