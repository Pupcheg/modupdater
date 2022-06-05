package me.supcheg.modupdater.common.mod;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Objects;
import java.util.StringJoiner;

public class ModInstance {

    private final String version;
    private final Path path;
    private final ModType type;

    public ModInstance(@NotNull String version, @NotNull Path path, @NotNull ModType type) {
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
