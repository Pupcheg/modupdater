package me.supcheg.modupdater.base.network;

import me.supcheg.modupdater.base.Updater;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.StringJoiner;

public abstract class ModDownloader {

    protected final String name;
    protected final String description;
    protected final Updater updater;

    protected ModDownloader(@NotNull String name, @NotNull String description, @NotNull Updater updater) {
        this.name = name;
        this.description = description;
        this.updater = updater;
    }

    public @NotNull Updater getUpdater() {
        return updater;
    }

    public @NotNull String getName() {
        return name;
    }

    public @NotNull String getDescription() {
        return description;
    }

    public boolean isAlwaysTrue() {
        return canDownload("");
    }

    public boolean canDownload(@NotNull String url) {
        return url.toLowerCase().contains(getName().toLowerCase());
    }

    public abstract @NotNull DownloadResult downloadLatest(@NotNull DownloadConfig downloadConfig);

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
}
