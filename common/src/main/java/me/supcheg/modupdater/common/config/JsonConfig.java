package me.supcheg.modupdater.common.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.supcheg.modupdater.common.util.Util;
import me.supcheg.modupdater.common.mod.Mod;
import me.supcheg.modupdater.common.mod.ModType;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.StringJoiner;

public class JsonConfig implements Config {

    protected final JsonObject jsonObject;
    protected final Path savePath;

    public JsonConfig(Path path) throws IOException {
        this.savePath = path;
        this.jsonObject = JsonParser.parseString(Files.readString(path)).getAsJsonObject();

        boolean b1 = jsonObject.getAsJsonObject("custom_urls") == null;
        boolean b2 = jsonObject.getAsJsonObject("specific_data") == null;
        if (b1) jsonObject.add("custom_urls", new JsonObject());
        if (b2) jsonObject.add("specific_data", new JsonObject());
        if (b1 || b2) save();
    }


    @NotNull
    @Override
    public Path getDownloadFolder() {
        return Path.of(Objects.requireNonNull(get("download_folder")));
    }

    @NotNull
    @Override
    public Path getModsFolder() {
        return Path.of(Objects.requireNonNull(get("mods_folder")));
    }

    @NotNull
    @Override
    public String getMinecraftVersion() {
        return Objects.requireNonNull(get("minecraft_version"));
    }

    @NotNull
    @Override
    public ModType getModsType() {
        return ModType.valueOf(Objects.requireNonNull(get("mods_type")).toUpperCase());
    }

    @Override
    public String get(@NotNull String address) {
        JsonElement el = jsonObject.get(address);
        return el == null ? null : el.getAsString();
    }

    @Override
    public void set(@NotNull String address, @NotNull String value) {
        jsonObject.addProperty(address, value);
        save();
    }

    @Override
    public void setCustomUrl(@NotNull Mod mod, @NotNull String value) {
        jsonObject.getAsJsonObject("custom_urls").addProperty(mod.getId(), value);
        save();
    }

    @Override
    public String getCustomUrl(@NotNull Mod mod) {
        JsonElement el = jsonObject.getAsJsonObject("custom_urls").get(mod.getId());
        return el == null ? null : el.getAsString();
    }

    @Override
    public void setSpecificData(@NotNull Mod mod, @NotNull String value) {
        jsonObject.getAsJsonObject("specific_data").addProperty(mod.getId(), value);
        save();
    }

    @Override
    public String getSpecificData(@NotNull Mod mod) {
        JsonElement el = jsonObject.getAsJsonObject("specific_data").get(mod.getId());
        return el == null ? null : el.getAsString();
    }

    public void save() {
        try {
            Files.writeString(savePath, Util.toPrettyString(jsonObject));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public @NotNull JsonObject getJsonObject() {
        return jsonObject;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", "JsonConfig{", "}")
                .add("jsonObject=" + jsonObject)
                .add("savePath=" + savePath)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JsonConfig that = (JsonConfig) o;
        return Objects.equals(jsonObject, that.jsonObject) && Objects.equals(savePath, that.savePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jsonObject, savePath);
    }
}
