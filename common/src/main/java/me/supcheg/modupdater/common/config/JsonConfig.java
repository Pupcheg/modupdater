package me.supcheg.modupdater.common.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.supcheg.modupdater.common.Updater;
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

    protected static final String DOWNLOAD_FOLDER_NAME = "download_folder";
    protected static final String MODS_FOLDER_NAME = "mods_folder";
    protected static final String MINECRAFT_VERSION_NAME = "minecraft_version";
    protected static final String MODS_TYPE_NAME = "mods_type";
    protected static final String URLS_NAME = "urls";

    protected final Updater updater;
    protected final JsonObject jsonObject;
    protected final Path savePath;

    public JsonConfig(@NotNull Updater updater, @NotNull Path path) throws IOException {
        this.updater = updater;
        this.savePath = path;
        this.jsonObject = JsonParser.parseString(Files.readString(path)).getAsJsonObject();

        if (jsonObject.getAsJsonObject(URLS_NAME) == null) {
            jsonObject.add(URLS_NAME, new JsonObject());
        }
    }

    @NotNull
    @Override
    public Updater getUpdater() {
        return updater;
    }

    @NotNull
    @Override
    public Path getDownloadFolder() {
        return Path.of(Objects.requireNonNull(get(DOWNLOAD_FOLDER_NAME)));
    }

    @NotNull
    @Override
    public Path getModsFolder() {
        return Path.of(Objects.requireNonNull(get(MODS_FOLDER_NAME)));
    }

    @NotNull
    @Override
    public String getMinecraftVersion() {
        return Objects.requireNonNull(get(MINECRAFT_VERSION_NAME));
    }

    @NotNull
    @Override
    public ModType getModsType() {
        return ModType.valueOf(Objects.requireNonNull(get(MODS_TYPE_NAME)).toUpperCase());
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
    public void setUrl(@NotNull Mod mod, @NotNull String value) {
        jsonObject.getAsJsonObject(URLS_NAME).addProperty(mod.getId(), value);
        save();
    }

    @Override
    public String getUrl(@NotNull Mod mod) {
        JsonElement el = jsonObject.getAsJsonObject(URLS_NAME).get(mod.getId());
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
