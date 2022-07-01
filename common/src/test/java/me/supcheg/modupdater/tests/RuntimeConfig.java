package me.supcheg.modupdater.tests;

import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;
import me.supcheg.modupdater.common.Updater;
import me.supcheg.modupdater.common.config.Config;
import me.supcheg.modupdater.common.mod.Mod;
import me.supcheg.modupdater.common.mod.ModType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class RuntimeConfig implements Config {

    private final Updater updater;
    private final Path downloadFolder;
    private final Path modsFolder;
    private final String minecraftVersion;
    private final ModType modsType;

    private final Map<String, String> mainMap;
    private final Map<String, String> urlsMap;

    public RuntimeConfig(@NotNull Updater updater, @NotNull String minecraftVersion, @NotNull ModType modsType) throws IOException {
        this.updater = updater;
        Path dir = Path.of(System.getProperty("user.dir"));
        this.downloadFolder = Files.createTempDirectory(dir, "downloads-");
        this.modsFolder = Files.createTempDirectory(dir, "mods-");
        this.minecraftVersion = minecraftVersion;
        this.modsType = modsType;
        this.mainMap = new HashMap<>();
        this.urlsMap = new HashMap<>();
    }

    @NotNull
    @Override
    public Updater getUpdater() {
        return updater;
    }

    @NotNull
    @Override
    public Path getDownloadFolder() {
        return downloadFolder;
    }

    @NotNull
    @Override
    public Path getModsFolder() {
        return modsFolder;
    }

    @NotNull
    @Override
    public String getMinecraftVersion() {
        return minecraftVersion;
    }

    @NotNull
    @Override
    public ModType getModsType() {
        return modsType;
    }

    @Nullable
    @Override
    public String get(@NotNull String address) {
        return mainMap.get(address);
    }

    @Override
    public void set(@NotNull String address, @NotNull String value) {
        mainMap.put(address, value);
    }

    @Override
    public void setUrl(@NotNull Mod mod, @NotNull String value) {
        urlsMap.put(mod.getId(), value);
    }

    @Nullable
    @Override
    public String getUrl(@NotNull Mod mod) {
        return urlsMap.get(mod.getId());
    }

    public void deleteDirectories() throws IOException {
        MoreFiles.deleteRecursively(this.downloadFolder, RecursiveDeleteOption.ALLOW_INSECURE);
        MoreFiles.deleteRecursively(this.modsFolder, RecursiveDeleteOption.ALLOW_INSECURE);
    }
}
