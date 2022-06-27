package me.supcheg.modupdater.common.util;

import me.supcheg.modupdater.common.Updater;
import me.supcheg.modupdater.common.comparator.VersionComparator;
import me.supcheg.modupdater.common.downloader.ModDownloader;
import me.supcheg.modupdater.common.mod.Mod;
import me.supcheg.modupdater.common.mod.ModInstance;
import me.supcheg.modupdater.common.searcher.DownloadUrlSearcher;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an object that somehow has {@link Updater} instance
 * @see ModDownloader
 * @see DownloadUrlSearcher
 * @see VersionComparator
 * @see Mod
 * @see ModInstance
 * @see DownloadConfig
 */
public interface UpdaterHolder {
    @NotNull
    Updater getUpdater();
}
