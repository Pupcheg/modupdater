package me.supcheg.modupdater.base.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.supcheg.modupdater.base.Util;
import me.supcheg.modupdater.base.mod.Mod;
import me.supcheg.modupdater.base.mod.ModType;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class JsonConfig implements Config {

    private final JsonObject jsonObject;
    private final Path savePath;

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
        jsonObject.getAsJsonObject("custom_urls").addProperty(mod.getName(), value);
        save();
    }

    @Override
    public String getCustomUrl(@NotNull Mod mod) {
        JsonElement el = jsonObject.getAsJsonObject("custom_urls").get(mod.getName());
        return el == null ? null : el.getAsString();
    }

    @Override
    public void setSpecificData(@NotNull Mod mod, @NotNull String value) {
        jsonObject.getAsJsonObject("specific_data").addProperty(mod.getName(), value);
        save();
    }

    @Override
    public String getSpecificData(@NotNull Mod mod) {
        JsonElement el = jsonObject.getAsJsonObject("specific_data").get(mod.getName());
        return el == null ? null : el.getAsString();
    }

    private void save() {
        try {
            Files.writeString(savePath, Util.toPrettyString(jsonObject));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return jsonObject.toString();
    }

    public JsonObject getObject() {
        return jsonObject;
    }
}
