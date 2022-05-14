package me.supcheg.modupdater.base.mod;

import me.supcheg.modupdater.base.Updater;
import me.supcheg.modupdater.base.network.ModDownloader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class Mod {

    private final Updater updater;
    private final String name;
    private String url;
    private String specificData;
    private ModDownloader downloader;
    private ModInstance latestInstance;
    private final Set<ModInstance> instances;

    public Mod(@NotNull Updater updater, @NotNull String name, @NotNull String url) {
        this.updater = updater;
        this.name = name;
        this.instances = new HashSet<>();
        this.url = url;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Mod.class.getSimpleName() + "{", "}")
                .add("name='" + name + "'")
                .add("url='" + url + "'")
                .add("specificData='" + specificData + "'")
                .add("downloader=" + downloader)
                .add("instances=" + instances)
                .toString();
    }

    public @NotNull ModInstance createInstance(@NotNull Path path) throws IOException {
        ModInstance instance = new ModInstance(path);
        instances.add(instance);
        if (latestInstance == null) latestInstance = instance;
        return instance;
    }

    public @NotNull Set<ModInstance> getInstances() {
        return instances;
    }

    public void setLatestInstance(@NotNull ModInstance latestInstance) {
        this.latestInstance = latestInstance;
    }

    public @NotNull ModInstance getLatestInstance() {
        return latestInstance;
    }

    public @NotNull String getName() {
        return name;
    }

    public @NotNull String getUrl() {
        return url;
    }

    public void setUrl(@NotNull String url) {
        this.url = url;
        setDownloader(this.updater.findModDownloaderForUrl(this.url, true));
    }

    public void setUrlWithOutChecks(String url) {
        this.url = url;
    }

    public void setDownloader(@NotNull ModDownloader downloader) {
        this.downloader = downloader;
    }

    public @NotNull ModDownloader getDownloader() {
        return downloader;
    }

    public @Nullable String getSpecificDownloadData() {
        return specificData;
    }

    public void setSpecificDownloadData(@Nullable String newSpecificData) {
        this.specificData = newSpecificData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Mod mod = (Mod) o;
        return Objects.equals(name, mod.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
