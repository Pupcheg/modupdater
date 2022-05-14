package me.supcheg.modupdater.base.mod;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public record SmallModInfo(@NotNull Collection<String> getSupportedMinecraftVersions,
                           @NotNull Collection<ModType> getSupportedModTypes) {}
