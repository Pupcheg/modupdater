package me.supcheg.modupdater.base.mod;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;

public record ModDescription(@NotNull String getUrl, @NotNull String getVersion, @NotNull String getName,
                             @NotNull ModType getType) {

    public static @NotNull ModDescription createFabric(@NotNull JsonObject obj) {
        String name = Optional.of(obj)
                .map(o -> o.get("id"))
                .map(JsonElement::getAsString)
                .orElse("");
        String version = Optional.of(obj)
                .map(o -> o.get("version"))
                .map(JsonElement::getAsString)
                .orElse("");

        String url = Optional.of(obj)
                .map(o -> o.get("contact"))
                .map(JsonElement::getAsJsonObject)
                .map(o -> o.get("homepage"))
                .map(JsonElement::getAsString)
                .orElse("");

        return new ModDescription(url, version, name, ModType.FABRIC);
    }

    public static @NotNull ModDescription createQuilt(@NotNull JsonObject obj) {
        return createFabric(obj.get("quilt_loader").getAsJsonObject());
    }

    public static @NotNull ModDescription createForge(@NotNull String[] manifest, @NotNull String[] mainFile) {
        String name = Arrays.stream(mainFile)
                .filter(s -> s.startsWith("modId"))
                .findFirst()
                .map(s -> s.substring(s.indexOf('"') + 1, s.lastIndexOf('"')))
                .orElse("");
        String version = Arrays.stream(manifest)
                .filter(s -> s.startsWith("Implementation-Version"))
                .findFirst()
                .map(s -> s.substring(s.indexOf(": ") + 2))
                .orElse("");

        String url = Arrays.stream(mainFile)
                .filter(s -> s.startsWith("displayURL"))
                .findFirst()
                .map(s -> s.substring(s.indexOf('"') + 1, s.lastIndexOf('"')))
                .orElse("");

        return new ModDescription(url, version, name, ModType.FORGE);
    }

}
