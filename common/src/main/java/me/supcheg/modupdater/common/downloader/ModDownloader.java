package me.supcheg.modupdater.common.downloader;

import me.supcheg.modupdater.common.Updater;
import me.supcheg.modupdater.common.concurrent.IntermediateResultAccessorFunction;
import me.supcheg.modupdater.common.concurrent.IntermediateResultProcess;
import me.supcheg.modupdater.common.util.DownloadConfig;
import me.supcheg.modupdater.common.util.DownloadResult;
import me.supcheg.modupdater.common.util.UpdaterHolder;
import me.supcheg.modupdater.common.util.Util;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public abstract class ModDownloader implements AutoCloseable, UpdaterHolder {

    @NotNull
    @Contract("_, _, _ -> new")
    public static ModDownloader from(@NotNull String name,
                                     @NotNull BiFunction<IntermediateResultProcess<String, DownloadResult>.IntermediateResultAccessor, DownloadConfig, DownloadResult> function,
                                     @NotNull Predicate<String> predicate) {
        return new SimpleModDownloader(name, function, predicate);
    }

    protected final String name;
    protected final Updater updater;

    // If updater is null, don't use it in #downloadLatest
    protected ModDownloader(@NotNull String name, @Nullable Updater updater) {
        this.name = name;
        this.updater = updater;
    }

    @Nullable
    @Override
    public Updater getUpdater() {
        return updater;
    }

    public @NotNull String getName() {
        return name;
    }

    public boolean canDownload(@NotNull String url) {
        return url.toLowerCase().contains(getName().toLowerCase());
    }

    public @NotNull IntermediateResultAccessorFunction<String, DownloadResult> createFunction(@NotNull DownloadConfig downloadConfig) {
        Util.validateSameUpdater(this, downloadConfig);
        return accessor -> downloadLatest(accessor, downloadConfig);
    }

    protected abstract @NotNull DownloadResult downloadLatest(@NotNull IntermediateResultProcess<String, DownloadResult>.IntermediateResultAccessor accessor,
                                                              @NotNull DownloadConfig downloadConfig);

    @Override
    public String toString() {
        return new StringJoiner(", ", ModDownloader.class.getSimpleName() + "{", "}")
                .add("name='" + name + "'")
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModDownloader that = (ModDownloader) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }


    @Override
    public void close() {}
}
