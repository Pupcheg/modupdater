package me.supcheg.modupdater.base;

import me.supcheg.modupdater.base.config.Config;
import me.supcheg.modupdater.base.mod.JarFileExplorer;
import me.supcheg.modupdater.base.mod.Mod;
import me.supcheg.modupdater.base.mod.ModDescription;
import me.supcheg.modupdater.base.network.ModDownloader;
import me.supcheg.modupdater.base.network.downloaders.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Updater {

    private final Map<String, ModDownloader> downloadersMap;
    private final Config config;
    private final Map<String, Mod> modsMap;
    private final SearchingModDownloader searchingModDownloader;

    public Updater(@NotNull Config config) {
        this.config = config;
        modsMap = new HashMap<>();
        downloadersMap = new HashMap<>();
        searchingModDownloader = new SearchingModDownloader(this);
        addDefaultModDownloaders();
        reloadDefaultModsFolder();
    }

    private void addDefaultModDownloaders() {
        addDownloader(new CurseForgeModDownloader());
        addDownloader(new GitHubModDownloader());
        addDownloader(new ModrinthModDownloader());
        addDownloader(searchingModDownloader);
        addDownloader(new UnknownModDownloader());
    }

    public void addDownloader(@NotNull ModDownloader modDownloader) {
        downloadersMap.put(modDownloader.getName().toLowerCase(), modDownloader);
    }

    public void reloadDefaultModsFolder() {
        Optional.ofNullable(config.get("mods_folder"))
                .map(Path::of)
                .ifPresent(path -> {
                    try {
                        loadMods(path);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    public void loadMods(@NotNull Path modsPath) throws IOException {
        modsMap.clear();
        //noinspection resource
        for (Path modPath : Files.list(modsPath).toList()) {
            ModDescription description = JarFileExplorer.getDescription(modPath);

            Mod mod = modsMap.get(description.getName().toLowerCase());
            if (mod == null) {
                String url = description.getUrl();
                mod = new Mod(this, description.getName(), url);

                String specific = config.getSpecificData(mod);
                mod.setSpecificDownloadData(specific);

                String customUrl = config.getCustomUrl(mod);
                if (customUrl != null) {
                    mod.setUrl(customUrl);
                } else {
                    mod.setDownloader(findModDownloaderForUrl(mod.getUrl(), specific != null));
                }
                modsMap.put(mod.getName().toLowerCase(), mod);
            }
            mod.createInstance(modPath);
        }
    }

    public @NotNull SearchingModDownloader getSearchingModDownloader() {
        return searchingModDownloader;
    }

    public ModDownloader findModDownloaderByName(@NotNull String name) {
        return downloadersMap.get(name.toLowerCase());
    }

    public @NotNull ModDownloader findModDownloaderForUrl(@NotNull String url, boolean allowCurseForge) {
        for (ModDownloader downloader : getModDownloaders()) {
            // check is always true
            if (!downloader.canDownload("") && downloader.canDownload(url)) {

                // CurseForge has download limit, try to find another downloader
                if (!allowCurseForge && downloader instanceof CurseForgeModDownloader) continue;

                return downloader;
            }
        }
        return searchingModDownloader;
    }

    public @NotNull ModDownloader findModDownloaderForUrl(@NotNull String url) {
        return findModDownloaderForUrl(url, false);
    }

    public @Nullable Mod findMod(@NotNull String name) {
        return modsMap.get(name.toLowerCase());
    }

    public @NotNull Collection<ModDownloader> getModDownloaders() {
        return downloadersMap.values();
    }

    public @NotNull Collection<Mod> getMods() {
        return modsMap.values();
    }

    public @NotNull Config getConfig() {
        return config;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Updater.class.getSimpleName() + "[", "]")
                .add("downloaders=" + downloadersMap)
                .add("mods=" + modsMap)
                .add("config=" + config)
                .toString();
    }
}
