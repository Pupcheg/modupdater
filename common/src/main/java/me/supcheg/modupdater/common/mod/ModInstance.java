package me.supcheg.modupdater.common.mod;

import me.supcheg.modupdater.common.Updater;
import me.supcheg.modupdater.common.util.UpdaterHolder;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Objects;
import java.util.StringJoiner;

public class ModInstance implements UpdaterHolder {

    private final Mod parent;
    private final String version;
    private final Path path;
    private final ModType type;

    public ModInstance(@NotNull Mod parent, @NotNull String version, @NotNull Path path, @NotNull ModType type) {
        this.parent = parent;
        this.version = version;
        this.path = path;
        this.type = type;
    }

    public @NotNull ModType getType() {
        return type;
    }

    public @NotNull Path getPath() {
        return path;
    }

    public @NotNull String getVersion() {
        return version;
    }

    public @NotNull Mod getParent() {
        return parent;
    }

    @NotNull
    @Override
    public Updater getUpdater() {
        return parent.getUpdater();
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", "ModInstance{", "}")
                .add("version='" + version + "'")
                .add("path=" + path)
                .add("type=" + type)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModInstance that = (ModInstance) o;
        return Objects.equals(version, that.version) && Objects.equals(path, that.path) && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, path, type);
    }
}
