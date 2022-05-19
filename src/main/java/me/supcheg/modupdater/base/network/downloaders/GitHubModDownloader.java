package me.supcheg.modupdater.base.network.downloaders;

import me.supcheg.modupdater.base.Updater;
import me.supcheg.modupdater.base.mod.Mod;
import me.supcheg.modupdater.base.network.DownloadConfig;
import me.supcheg.modupdater.base.network.DownloadResult;
import me.supcheg.modupdater.base.network.ModDownloader;
import org.jetbrains.annotations.NotNull;
import org.kohsuke.github.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class GitHubModDownloader extends ModDownloader {

    private volatile GitHub github;

    public GitHubModDownloader(@NotNull Updater updater) {
        super("GitHub", "Can download mods from https://github.com. Specific download data: repo_name.", updater);
    }

    public void setToken(@NotNull String token) {
        synchronized (this) {
            try {
                github = GitHubBuilder.fromEnvironment().withOAuthToken(token).build();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private @NotNull GitHub getGithub() {
        if (github == null) {
            synchronized (this) {
                if (github == null) {
                    throw new IllegalStateException("GitHub token is not set");
                }
            }
        }
        return github;
    }

    @NotNull
    @Override
    public DownloadResult downloadLatest(@NotNull DownloadConfig downloadConfig) {
        try {
            Mod mod = downloadConfig.getMod();
            String repoName = mod.getSpecificDownloadData() == null ? mod.getUrl().substring("https://github.com/".length()) : mod.getSpecificDownloadData();

            GHRepository repo = getGithub().getRepository(repoName);

            for (GHArtifact artifact : repo.listArtifacts().toList()) {
                DownloadResult result = artifact.download(i -> downloadFromArtifact(mod, downloadConfig, i));
                if (result.isSuccess()) {
                    return result;
                }
            }

            GHRelease release = repo.getLatestRelease();
            if (release != null) {
                Optional<GHAsset> assetOptional = release.listAssets().toList().stream()
                        .filter(g -> downloadConfig.isCorrect(g.getName()))
                        .filter(a -> !a.getName().contains("source"))
                        .findFirst();

                if (assetOptional.isPresent()) {
                    GHAsset asset = assetOptional.get();
                    Path download = downloadConfig.getDownloadFolder().resolve(asset.getName());
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

    private @NotNull DownloadResult downloadFromArtifact(@NotNull Mod mod, @NotNull DownloadConfig downloadConfig, @NotNull InputStream from) throws IOException {
        Path temp = downloadConfig.getDownloadFolder().resolve(Integer.toHexString(ThreadLocalRandom.current().nextInt()));
        Files.copy(from, temp, StandardCopyOption.REPLACE_EXISTING);

        ZipEntry entry;
        try (ZipInputStream in = new ZipInputStream(new FileInputStream(temp.toFile()))) {
            while ((entry = in.getNextEntry()) != null) {
                String name = entry.getName();
                if (downloadConfig.isCorrect(name) &&
                        !name.contains("sources") &&
                        !name.contains("dev") &&
                        !name.contains("api")) {
                    break;
                }
            }
        }

        if (entry == null) {
            Files.delete(temp);
            return DownloadResult.createError("A suitable file was not found in the archive.");
        }

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
