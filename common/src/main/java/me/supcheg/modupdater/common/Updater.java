package me.supcheg.modupdater.common;

import me.supcheg.modupdater.common.comparator.DefaultVersionComparator;
import me.supcheg.modupdater.common.comparator.VersionComparator;
import me.supcheg.modupdater.common.concurrent.ChangeConsumer;
import me.supcheg.modupdater.common.concurrent.IntermediateResultProcess;
import me.supcheg.modupdater.common.config.Config;
import me.supcheg.modupdater.common.downloader.*;
import me.supcheg.modupdater.common.mod.Mod;
import me.supcheg.modupdater.common.searcher.DefaultDownloadUrlSearcher;
import me.supcheg.modupdater.common.searcher.DownloadUrlSearcher;
import me.supcheg.modupdater.common.util.DownloadConfig;
import me.supcheg.modupdater.common.util.DownloadResult;
import me.supcheg.modupdater.common.util.JarFileDescription;
import me.supcheg.modupdater.common.util.JarFileExplorer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Updater implements AutoCloseable {

    public static @NotNull Builder builder() {
        return new Builder();
    }

    private final ExecutorService downloadExecutor;
    private final Function<Exception, DownloadResult> exceptionDownloadResultFunction;
    private final Map<String, ModDownloader> downloadersMap;
    private final Config config;
    private final Map<String, Mod> modsMap;
    private DownloadUrlSearcher urlSearcher;
    private VersionComparator versionComparator;

    private Updater(@NotNull Builder builder) {
        this.downloadExecutor = builder.executor;
        this.exceptionDownloadResultFunction = DownloadResult::createError;
        this.downloadersMap = new ConcurrentHashMap<>();
        for (Function<Updater, ModDownloader> f : builder.downloaders) {
            addDownloader(f.apply(this));
        }
        this.config = builder.config;
        this.modsMap = new ConcurrentHashMap<>();
        this.urlSearcher = builder.urlSearcher;
        this.versionComparator = builder.versionComparator;
    }

    public void addDownloader(@NotNull ModDownloader modDownloader) {
        this.downloadersMap.put(modDownloader.getName().toLowerCase(), modDownloader);
    }

    public void reloadDefaultModsFolder() {
        try {
            loadMods(this.config.getModsFolder());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadMods(@NotNull Path modsPath) throws IOException {
        this.modsMap.clear();
        Set<Path> mods;
        try (var stream = Files.list(modsPath)) {
            mods = stream.filter(path -> path.toString().endsWith(".jar")).collect(Collectors.toSet());
        }
        for (Path modPath : mods) {
            JarFileDescription description = JarFileExplorer.getDescription(modPath);

            Mod mod = this.modsMap.get(description.getId().toLowerCase());
            if (mod == null) {
                mod = new Mod(this, description.getName(), description.getId());

                mod.setDownloader(findModDownloaderForMod(mod));

                this.modsMap.put(mod.getId().toLowerCase(), mod);
            }
            mod.createInstance(modPath, description);
        }
    }

    public @NotNull DownloadUrlSearcher getUrlSearcher() {
        return urlSearcher;
    }

    public void setUrlSearcher(@NotNull DownloadUrlSearcher urlSearcher) {
        this.urlSearcher = urlSearcher;
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

    public @NotNull ModDownloader findModDownloaderForUrl(@NotNull String url) {
        for (ModDownloader downloader : getModDownloaders()) {
            if (downloader.canDownload(url)) {
                return downloader;
            }
        }

        throw new UnsupportedOperationException();
    }

    public @NotNull ModDownloader findModDownloaderForMod(@NotNull Mod mod) {
        String url = mod.getUrl();
        if (url == null) {
            url = urlSearcher.find(mod);
            if (url == null) {
                url = "";
            }
            mod.setUrl(url);
        }

        return findModDownloaderForUrl(url);
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

    public @NotNull IntermediateResultProcess<String, DownloadResult> downloadLatest(@NotNull DownloadConfig downloadConfig,
                                                                                     @Nullable ChangeConsumer<String> onChange,
                                                                                     @Nullable Consumer<DownloadResult> after) {
        return IntermediateResultProcess.builder(String.class, DownloadResult.class)
                .executor(this.downloadExecutor)
                .function(downloadConfig.getMod().getDownloader().createFunction(downloadConfig))
                .defaultResult(this.exceptionDownloadResultFunction)
                .onChange(onChange)
                .after(after)
                .run();
    }

    @Override
    public void close() {
        for (ModDownloader downloader : downloadersMap.values()) {
            downloader.close();
        }
        downloadExecutor.shutdownNow();
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


    public static class Builder {

        private final Set<Function<Updater, ModDownloader>> downloaders;
        private DownloadUrlSearcher urlSearcher;
        private VersionComparator versionComparator;
        private Config config;
        private ExecutorService executor;

        private Builder() {
            downloaders = new HashSet<>();
        }

        public @NotNull Builder urlSearcher(@NotNull DownloadUrlSearcher urlSearcher) {
            this.urlSearcher = urlSearcher;
            return this;
        }

        public @NotNull Builder versionComparator(@NotNull VersionComparator versionComparator) {
            this.versionComparator = versionComparator;
            return this;
        }

        public @NotNull Builder config(@NotNull Config config) {
            this.config = config;
            return this;
        }

        public @NotNull Builder executor(@NotNull ExecutorService executor) {
            this.executor = executor;
            return this;
        }

        public @NotNull Builder addDownloader(@NotNull Function<Updater, ModDownloader> downloader) {
            this.downloaders.add(downloader);
            return this;
        }

        public @NotNull Builder addDownloader(@NotNull ModDownloader downloader) {
            return addDownloader(u -> downloader);
        }

        public @NotNull Builder defaultUrlSearcher() {
            return urlSearcher(new DefaultDownloadUrlSearcher());
        }

        public @NotNull Builder defaultVersionComparator() {
            return versionComparator(new DefaultVersionComparator());
        }

        public @NotNull Builder defaultDownloaders() {
            addDownloader(CurseForgeModDownloader::new);
            addDownloader(GitHubModDownloader::new);
            addDownloader(ModrinthModDownloader::new);
            addDownloader(UnknownModDownloader::new);
            return this;
        }

        public @NotNull Builder defaultExecutor() {
            return executor(Executors.newSingleThreadExecutor());
        }


        public @NotNull Updater build() {
            Objects.requireNonNull(urlSearcher);
            Objects.requireNonNull(versionComparator);
            if (downloaders.isEmpty()) {
                throw new IllegalStateException("Downloaders should not be empty");
            }
            Objects.requireNonNull(config);
            Objects.requireNonNull(executor);

            return new Updater(this);
        }
    }
}
