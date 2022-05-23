package me.supcheg.modupdater.base;

import me.supcheg.modupdater.base.comparator.DefaultVersionComparator;
import me.supcheg.modupdater.base.comparator.VersionComparator;
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
    private SearchingModDownloader searchingModDownloader;
    private VersionComparator versionComparator;

    public Updater(@NotNull Config config) {
        this.config = config;
        modsMap = new HashMap<>();
        downloadersMap = new HashMap<>();

        // Defaults
        searchingModDownloader = new SearchingModDownloader(this);
        versionComparator = new DefaultVersionComparator();

        addDownloader(new CurseForgeModDownloader(this));
        addDownloader(new GitHubModDownloader(this));
        addDownloader(new ModrinthModDownloader(this));
        addDownloader(searchingModDownloader);
        addDownloader(new UnknownModDownloader(this));

        reloadDefaultModsFolder();
    }

    public void addDownloader(@NotNull ModDownloader modDownloader) {
        downloadersMap.put(modDownloader.getName().toLowerCase(), modDownloader);
    }

    public void reloadDefaultModsFolder() {
        try {
            loadMods(config.getModsFolder());
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public void setSearchingModDownloader(@NotNull SearchingModDownloader searchingModDownloader) {
        var old = this.searchingModDownloader;

        this.searchingModDownloader = searchingModDownloader;

        downloadersMap.remove(old.getName().toLowerCase());
        addDownloader(this.searchingModDownloader);
    }

    public @NotNull VersionComparator getVersionComparator() {
        return versionComparator;
    }

    public void setVersionComparator(@NotNull VersionComparator versionComparator) {
        this.versionComparator = versionComparator;
    }

    public ModDownloader findModDownloaderByName(@NotNull String name) {
        return downloadersMap.get(name.toLowerCase());
    }

    public @NotNull ModDownloader findModDownloaderForUrl(@NotNull String url, boolean allowCurseForge) {
        for (ModDownloader downloader : getModDownloaders()) {
            if (!downloader.isAlwaysTrue() && downloader.canDownload(url)) {
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
                .add("versionComparator=" + versionComparator)
                .toString();
    }
}
