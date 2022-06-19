package me.supcheg.modupdater.common.downloader;

import me.supcheg.modupdater.common.Updater;
import me.supcheg.modupdater.common.util.Util;
import me.supcheg.modupdater.common.concurrent.IntermediateResultProcess;
import me.supcheg.modupdater.common.mod.Mod;
import me.supcheg.modupdater.common.util.DownloadConfig;
import me.supcheg.modupdater.common.util.DownloadResult;
import org.jetbrains.annotations.NotNull;
import org.kohsuke.github.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class GitHubModDownloader extends ModDownloader {

    private final Predicate<String> notContainsPredicate;
    private volatile GitHub github;

    public GitHubModDownloader(@NotNull Updater updater) {
        super("GitHub", updater);

        var blacklist = Set.of("source", "src", "dev", "api");
        notContainsPredicate = s -> {
            s = s.trim().toLowerCase();
            for (String black : blacklist) {
                if (s.contains(black))
                    return false;
            }
            return true;
        };
    }

    private @NotNull GitHub getGithub() {
        if (github == null) {
            synchronized (this) {
                if (github == null) {
                    try {
                        String token = updater.getConfig().get("github_token");
                        if (token == null) {
                            throw new IllegalStateException("'github_token' is not set in the config");
                        }
                        github = GitHubBuilder.fromEnvironment().withOAuthToken(token).build();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return github;
    }

    @NotNull
    @Override
    protected DownloadResult downloadLatest(@NotNull IntermediateResultProcess<String, DownloadResult>.IntermediateResultAccessor accessor,
                                            @NotNull DownloadConfig downloadConfig) {
        try {
            Mod mod = downloadConfig.getMod();
            String repoName = Objects.requireNonNull(mod.getUrl()).substring("https://github.com/".length());

            GHRepository repo = getGithub().getRepository(repoName);

            for (GHArtifact artifact : repo.listArtifacts()) {
                DownloadResult result = artifact.download(i -> downloadFromArtifact(accessor, mod, downloadConfig, i));
                if (result.isSuccess()) {
                    return result;
                }
            }

            for (GHRelease release : repo.listReleases()) {
                Optional<GHAsset> assetOptional = Util.stream(release.listAssets())
                        .filter(g -> downloadConfig.isCorrect(g.getName()))
                        .filter(a -> notContainsPredicate.test(a.getName()))
                        .findFirst();

                if (assetOptional.isPresent()) {
                    GHAsset asset = assetOptional.get();
                    Path download = downloadConfig.getDownloadFolder().resolve(asset.getName());

                    accessor.set("Downloading release");
                    try (InputStream in = new URL(asset.getBrowserDownloadUrl()).openStream()) {
                        Files.copy(in, download, StandardCopyOption.REPLACE_EXISTING);
                    }
                    return DownloadResult.createSuccess(mod.createInstance(download));
                }
            }

            return DownloadResult.createError("The repository is empty.");
        } catch (Exception e) {
            return DownloadResult.createError(e);
        }
    }

    private @NotNull DownloadResult downloadFromArtifact(@NotNull IntermediateResultProcess<String, DownloadResult>.IntermediateResultAccessor accessor,
                                                         @NotNull Mod mod, @NotNull DownloadConfig downloadConfig, @NotNull InputStream from) throws IOException {
        Path temp = downloadConfig.getDownloadFolder().resolve(Integer.toHexString(ThreadLocalRandom.current().nextInt()));
        accessor.set("Downloading artifact");
        Files.copy(from, temp, StandardCopyOption.REPLACE_EXISTING);

        ZipEntry entry;
        try (ZipInputStream in = new ZipInputStream(new FileInputStream(temp.toFile()))) {
            while ((entry = in.getNextEntry()) != null) {
                String name = entry.getName();
                if (downloadConfig.isCorrect(name) && notContainsPredicate.test(name)) {
                    break;
                }
            }
        }

        if (entry == null) {
            Files.delete(temp);
            return DownloadResult.createError("A suitable file was not found in the archive.");
        }

        accessor.set("Unzipping file");

        String entryName = entry.getName();
        int x = entryName.lastIndexOf('\\');
        int index = x == -1 ? entryName.lastIndexOf('/') : x;

        Path download = downloadConfig.getDownloadFolder().resolve(entryName.substring(index + 1));
        try (ZipFile file = new ZipFile(temp.toFile())) {
            try (InputStream inputStream = file.getInputStream(entry)) {
                Files.copy(inputStream, download, StandardCopyOption.REPLACE_EXISTING);
            }
        }

        Files.delete(temp);
        return DownloadResult.createSuccess(mod.createInstance(download));
    }
}
