package me.supcheg.modupdater.base.mod;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.StringJoiner;

public class ModInstance {

    private final String version;
    private final Path path;
    private final ModType type;

    public ModInstance(@NotNull Path path) throws IOException {
        this.path = path;

        ModDescription description = JarFileExplorer.getDescription(path);
        this.version = description.getVersion();
        this.type = description.getType();
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
        return new StringJoiner(", ", ModInstance.class.getSimpleName() + "{", "}")
                .add("version='" + version + "'")
                .add("path=" + path)
                .add("type=" + type)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModInstance instance = (ModInstance) o;
        return Objects.equals(version, instance.version) && type == instance.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, path, type);
    }
}
