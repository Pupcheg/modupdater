package me.supcheg.modupdater.common.searcher;

import me.supcheg.modupdater.common.mod.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface DownloadUrlSearcher {
    @Nullable
    String find(@NotNull Mod mod);
}
