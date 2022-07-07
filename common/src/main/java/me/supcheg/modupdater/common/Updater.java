package me.supcheg.modupdater.common;

import me.supcheg.modupdater.common.comparator.DefaultVersionComparator;
import me.supcheg.modupdater.common.comparator.VersionComparator;
import me.supcheg.modupdater.common.concurrent.ChangeConsumer;
import me.supcheg.modupdater.common.concurrent.IntermediateResultProcess;
import me.supcheg.modupdater.common.config.Config;
import me.supcheg.modupdater.common.downloader.*;
import me.supcheg.modupdater.common.mod.Mod;
import me.supcheg.modupdater.common.searcher.CombinedDownloadUrlSearcher;
import me.supcheg.modupdater.common.searcher.DefaultDownloadUrlSearcher;
import me.supcheg.modupdater.common.searcher.DownloadUrlSearcher;
import me.supcheg.modupdater.common.searcher.RemoteLibraryDownloadUrlSearcher;
import me.supcheg.modupdater.common.util.*;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Updater implements AutoCloseable, UpdaterHolder {

    public static @NotNull Builder builder() {
        return new Builder();
    }

    private final ExecutorService downloadExecutor;
    private final Function<Exception, DownloadResult> exceptionDownloadResultFunction;
    private final Map<String, ModDownloader> downloadersMap;
    private final Config config;
    private final Map<String, Mod> modsMap;
    private OkHttpClient httpClient;
    private DownloadUrlSearcher urlSearcher;
    private VersionComparator versionComparator;

    private Updater(@NotNull Builder builder) {
        this.httpClient = builder.httpClient;
        this.downloadExecutor = builder.executor;
        this.exceptionDownloadResultFunction = DownloadResult::createError;
        this.downloadersMap = new ConcurrentHashMap<>();
        for (Function<Updater, ModDownloader> f : builder.downloaders) {
            addDownloader(f.apply(this));
        }
        this.config = builder.config.apply(this);
        this.modsMap = new ConcurrentHashMap<>();

        var searchers = builder.urlSearchers;
        if (searchers.size() == 1) {
            this.urlSearcher = searchers.getFirst().apply(this);
        } else {
            CombinedDownloadUrlSearcher combined = new CombinedDownloadUrlSearcher(this);
            for (var func : searchers) {
                combined.addLast(func.apply(this));
            }
            this.urlSearcher = combined;
        }

        this.versionComparator = builder.versionComparator.apply(this);
    }

    public @NotNull OkHttpClient getHttpClient() {
        return httpClient;
    }

    public void setHttpClient(@NotNull OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void addDownloader(@NotNull ModDownloader modDownloader) {
        Util.validateSameUpdater(this, modDownloader);
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
        Util.validateSameUpdater(this, urlSearcher);
        this.urlSearcher = urlSearcher;
    }

    public @NotNull VersionComparator getVersionComparator() {
        return versionComparator;
    }

    public void setVersionComparator(@NotNull VersionComparator versionComparator) {
        Util.validateSameUpdater(this, versionComparator);
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
        Util.validateSameUpdater(this, mod);
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

    public @NotNull ExecutorService getDownloadExecutor() {
        return downloadExecutor;
    }

    public @NotNull IntermediateResultProcess<String, DownloadResult> downloadLatest(@NotNull DownloadConfig downloadConfig,
                                                                                     @Nullable ChangeConsumer<String> onChange,
                                                                                     @Nullable Consumer<DownloadResult> after) {
        return IntermediateResultProcess.<String, DownloadResult>builder()
                .executor(this.downloadExecutor)
                .function(downloadConfig.getMod().getDownloader().createFunction(downloadConfig))
                .defaultResult(this.exceptionDownloadResultFunction)
                .onChange(onChange)
                .after(after)
                .run();
    }

    @NotNull
    @Override
    public Updater getUpdater() {
        return this;
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
        private final Deque<Function<Updater, DownloadUrlSearcher>> urlSearchers;
        private OkHttpClient httpClient;
        private Function<Updater, VersionComparator> versionComparator;
        private Function<Updater, Config> config;
        private ExecutorService executor;

        private Builder() {
            downloaders = new HashSet<>();
            urlSearchers = new ArrayDeque<>();
        }

        public @NotNull Builder addUrlSearcher(@NotNull Function<Updater, DownloadUrlSearcher> urlSearcher) {
            this.urlSearchers.addLast(urlSearcher);
            return this;
        }

        public @NotNull Builder versionComparator(@NotNull Function<Updater, VersionComparator> versionComparator) {
            this.versionComparator = versionComparator;
            return this;
        }

        public @NotNull Builder httpClient(@NotNull OkHttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        public @NotNull Builder config(@NotNull Function<Updater, Config> config) {
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

        public @NotNull Builder defaultUrlSearchers() {
            return addUrlSearcher(RemoteLibraryDownloadUrlSearcher::new)
                    .addUrlSearcher(DefaultDownloadUrlSearcher::new);
        }

        public @NotNull Builder defaultVersionComparator() {
            return versionComparator(DefaultVersionComparator::new);
        }

        public @NotNull Builder defaultHttpClient() {
            Duration defaultTimeout = Duration.ofMillis(5000);

            return httpClient(new OkHttpClient.Builder()
                    .connectTimeout(defaultTimeout)
                    .readTimeout(defaultTimeout)
                    .writeTimeout(defaultTimeout)
                    .retryOnConnectionFailure(true)
                    .build());
        }

        public @NotNull Builder defaultDownloaders() {
            addDownloader(CurseForgeModDownloader::new);
            addDownloader(GitHubModDownloader::new);
            addDownloader(ModrinthModDownloader::new);
            addDownloader(UnknownModDownloader::new);
            return this;
        }

        public @NotNull Builder defaultExecutor() {
            return executor(Executors.newFixedThreadPool(3));
        }


        public @NotNull Updater build() {
            Objects.requireNonNull(httpClient);
            Objects.requireNonNull(versionComparator);
            if (urlSearchers.isEmpty()) {
                throw new IllegalStateException("UrlSearchers should not be empty");
            }
            if (downloaders.isEmpty()) {
                throw new IllegalStateException("Downloaders should not be empty");
            }
            Objects.requireNonNull(config);
            Objects.requireNonNull(executor);

            return new Updater(this);
        }
    }
}
