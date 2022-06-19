package me.supcheg.modupdater.common.searcher;

import me.supcheg.modupdater.common.Updater;
import me.supcheg.modupdater.common.mod.Mod;
import me.supcheg.modupdater.common.util.UpdaterHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface DownloadUrlSearcher extends UpdaterHolder {
    @Nullable
    String find(@NotNull Mod mod);

    @Nullable
    @Override
    default Updater getUpdater() {
        return null;
    }
}
