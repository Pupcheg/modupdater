package me.supcheg.modupdater.common.downloader;

import me.supcheg.modupdater.common.Updater;
import me.supcheg.modupdater.common.concurrent.IntermediateResultAccessorFunction;
import me.supcheg.modupdater.common.concurrent.IntermediateResultProcess;
import me.supcheg.modupdater.common.util.DownloadConfig;
import me.supcheg.modupdater.common.util.DownloadResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.StringJoiner;

public abstract class ModDownloader implements AutoCloseable {

    protected final String name;
    protected final String description;
    protected final Updater updater;

    // If updater is null, don't use it in #downloadLatest
    protected ModDownloader(@NotNull String name, @Nullable Updater updater) {
        this(name, "", updater);
    }

    protected ModDownloader(@NotNull String name, @NotNull String description, @Nullable Updater updater) {
        this.name = name;
        this.description = description;
        this.updater = updater;
    }

    public @NotNull String getName() {
        return name;
    }

    public @NotNull String getDescription() {
        return description;
    }

    public boolean canDownload(@NotNull String url) {
        return url.toLowerCase().contains(getName().toLowerCase());
    }

    public @NotNull IntermediateResultAccessorFunction<String, DownloadResult> createFunction(@NotNull DownloadConfig downloadConfig) {
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
