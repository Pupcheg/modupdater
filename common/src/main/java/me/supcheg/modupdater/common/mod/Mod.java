package me.supcheg.modupdater.common.mod;

import me.supcheg.modupdater.common.Updater;
import me.supcheg.modupdater.common.downloader.ModDownloader;
import me.supcheg.modupdater.common.util.JarFileDescription;
import me.supcheg.modupdater.common.util.JarFileExplorer;
import me.supcheg.modupdater.common.util.UpdaterHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class Mod implements UpdaterHolder {

    private final Updater updater;

    private final String id;
    private final String name;

    private final SortedSet<ModInstance> instances;
    private ModDownloader downloader;

    public Mod(@NotNull Updater updater, @NotNull String name, @NotNull String id) {
        this.updater = updater;

        this.name = name;
        this.id = id;
        Comparator<ModInstance> comparator = updater.getVersionComparator().transform(ModInstance::getVersion);
        this.instances = new TreeSet<>(comparator);
    }

    public @NotNull ModInstance createInstance(@NotNull Path path, @NotNull JarFileDescription description) {
        ModInstance instance = new ModInstance(this, description.getVersion(), path, description.getModType());
        instances.add(instance);
        return instance;
    }

    public @NotNull ModInstance createInstance(@NotNull Path path) throws IOException {
        return createInstance(path, JarFileExplorer.getDescription(path));
    }

    public @NotNull SortedSet<ModInstance> getInstances() {
        return instances;
    }

    public @NotNull ModInstance getLatestInstance() {
        return instances.first();
    }

    public @NotNull String getName() {
        return name;
    }

    public @NotNull String getId() {
        return id;
    }

    public @Nullable String getUrl() {
        return updater.getConfig().getCustomUrl(this);
    }

    public void setUrl(@NotNull String url) {
        updater.getConfig().setCustomUrl(this, url);
    }

    public void setDownloader(@NotNull ModDownloader downloader) {
        this.downloader = downloader;
    }

    public @NotNull ModDownloader getDownloader() {
        return downloader;
    }

    @Nullable
    @Override
    public Updater getUpdater() {
        return updater;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", "Mod{", "}")
                .add("name='" + name + "'")
                .add("id='" + id + "'")
                .add("downloader=" + downloader)
                .add("instances=" + instances)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Mod mod = (Mod) o;
        return Objects.equals(id, mod.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
