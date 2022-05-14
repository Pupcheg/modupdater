package me.supcheg.modupdater.base.network;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.StringJoiner;

public abstract class ModDownloader {

    private final String name;
    private final String description;

    protected ModDownloader(@NotNull String name, @NotNull String description) {
        this.name = name;
        this.description = description;
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
