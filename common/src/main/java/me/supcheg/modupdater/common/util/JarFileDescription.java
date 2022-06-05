package me.supcheg.modupdater.common.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.supcheg.modupdater.common.mod.ModType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

public class JarFileDescription {

    public static @NotNull JarFileDescription createFabric(@NotNull JsonObject obj) {
        String id = Optional.of(obj)
                .map(o -> o.get("id"))
                .map(JsonElement::getAsString)
                .orElse("");
        String name = Optional.of(obj)
                .map(o -> o.get("name"))
                .map(JsonElement::getAsString)
                .orElse("");
        String version = Optional.of(obj)
                .map(o -> o.get("version"))
                .map(JsonElement::getAsString)
                .orElse("");

        return new JarFileDescription(name, id, version, ModType.FABRIC);
    }

    public static @NotNull JarFileDescription createQuilt(@NotNull JsonObject obj) {
        return createFabric(obj.get("quilt_loader").getAsJsonObject());
    }

    public static @NotNull JarFileDescription createForge(@NotNull String[] manifest, @NotNull String[] mainFile) {
        String id = Arrays.stream(mainFile)
                .filter(s -> s.startsWith("modId"))
                .findFirst()
                .map(s -> s.substring(s.indexOf('"') + 1, s.lastIndexOf('"')))
                .orElse("");
        String version = Arrays.stream(manifest)
                .filter(s -> s.startsWith("Implementation-Version"))
                .findFirst()
                .map(s -> s.substring(s.indexOf(": ") + 2))
                .orElse("");

        return new JarFileDescription(id, id, version, ModType.FORGE);
    }


    private final String name, id, version;
    private final ModType modType;

    private JarFileDescription(String name, String id, String version, ModType modType) {
        this.name = name;
        this.id = id;
        this.version = version;
        this.modType = modType;
    }

    public @NotNull String getName() {
        return name;
    }

    public @NotNull String getId() {
        return id;
    }

    public @NotNull String getVersion() {
        return version;
    }

    public @NotNull ModType getModType() {
        return modType;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", "JarFileDescription{", "}")
                .add("name='" + name + "'")
                .add("id='" + id + "'")
                .add("version='" + version + "'")
                .add("modType=" + modType)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JarFileDescription that = (JarFileDescription) o;
        return Objects.equals(name, that.name) && Objects.equals(id, that.id) && Objects.equals(version, that.version) && modType == that.modType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, id, version, modType);
    }
}
